package com.datathon.api.assessment;

import com.datathon.api.organization.OrganizationType;
import com.datathon.api.wording.ForbiddenTerms;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Deterministic check after every model call (docs/ANOMALY_DETECTION.md §4.4). Additions to §4.4, all stricter
 * (work/decisions.md, #44): a claim over 300 characters is dropped (§4.3 maxLength); a forbidden term in an
 * explanation or in missing data rejects the whole answer, because those texts are shown too; "visoka_pozornost" with
 * fewer than two accepted reasons becomes "vrijedi_provjeriti" (prompt rule 7: two independent signals); a claim
 * about peer municipalities is dropped when the context has no peer statistics (prompt rule 9 of ai-flag-v2).
 */
public final class AiAssessmentValidator {

    static final int MAX_CLAIM_LENGTH = 300;
    private static final int MIN_REASONS_FOR_HIGH_ATTENTION = 2;
    /** Words of a peer comparison (usporedne općine, usporedba, medijan); allowed only with peer statistics. */
    private static final List<String> PEER_COMPARISON_STEMS = List.of("usporedn", "usporedb", "medijan");
    private static final String PEER_STATISTICS_PATH = "statistika_skupine";

    /** Words of a craft name that are not part of a person's name (compared without diacritics). */
    private static final List<String> CRAFT_WORDS = List.of("obrt", "usluge", "usluga", "trgovina", "vlasnik",
            "vlasnica", "gradevinski", "prijevoz", "servis", "ugostiteljski", "zanatski", "proizvodnju", "proizvodnja");

    private AiAssessmentValidator() {
    }

    public static AiValidation validate(AiAnswer answer, AiContext context) {
        Optional<AiLabel> label = AiLabel.fromCode(answer.label());
        Optional<AiConfidence> confidence = AiConfidence.fromCode(answer.confidence());
        if (label.isEmpty() || confidence.isEmpty()) {
            return rejected(answer, AiLabel.NO_FLAG, "nepoznata oznaka ili pouzdanost");
        }
        Optional<String> wholeAnswerProblem = wholeAnswerProblem(answer, label.get());
        if (wholeAnswerProblem.isPresent()) {
            return rejected(answer, label.get(), wholeAnswerProblem.get());
        }
        List<AiAnswer.Reason> accepted = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        for (int index = 0; index < answer.reasonsOrEmpty().size(); index++) {
            AiAnswer.Reason reason = answer.reasonsOrEmpty().get(index);
            Optional<String> problem = reasonProblem(reason, context);
            if (problem.isPresent()) {
                problems.add("razlog " + (index + 1) + ": " + problem.get());
            } else {
                accepted.add(reason);
            }
        }
        if (!answer.reasonsOrEmpty().isEmpty() && accepted.isEmpty()) {
            return new AiValidation(AiAssessmentStatus.REJECTED, AiLabel.NO_FLAG, confidence.get(),
                    answer.reasonsOrEmpty(), answer.missingDataOrEmpty(), answer.legitExplanationsOrEmpty(),
                    "svi razlozi odbačeni; " + String.join("; ", problems));
        }
        AiLabel finalLabel = label.get();
        if (finalLabel == AiLabel.HIGH_ATTENTION && accepted.size() < MIN_REASONS_FOR_HIGH_ATTENTION) {
            finalLabel = AiLabel.WORTH_CHECKING;
            problems.add("visoka_pozornost s manje od dva prihvaćena razloga, spušteno na vrijedi_provjeriti");
        }
        return new AiValidation(AiAssessmentStatus.ACCEPTED, finalLabel, confidence.get(), accepted,
                answer.missingDataOrEmpty(), answer.legitExplanationsOrEmpty(),
                problems.isEmpty() ? null : String.join("; ", problems));
    }

    private static Optional<String> wholeAnswerProblem(AiAnswer answer, AiLabel label) {
        if (answer.legitExplanationsOrEmpty().stream().allMatch(AiAssessmentValidator::isBlank)) {
            return Optional.of("nema mogućeg legitimnog objašnjenja");
        }
        if (label != AiLabel.NO_FLAG && answer.reasonsOrEmpty().isEmpty()) {
            return Optional.of("oznaka " + label.code() + " bez razloga");
        }
        List<String> shownTexts = new ArrayList<>(answer.legitExplanationsOrEmpty());
        shownTexts.addAll(answer.missingDataOrEmpty());
        for (String text : shownTexts) {
            List<String> found = ForbiddenTerms.findIn(text);
            if (!found.isEmpty()) {
                return Optional.of("zabranjena riječ u objašnjenju ili popisu podataka: " + found);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> reasonProblem(AiAnswer.Reason reason, AiContext context) {
        if (isBlank(reason.claim())) {
            return Optional.of("prazna tvrdnja");
        }
        if (reason.claim().length() > MAX_CLAIM_LENGTH) {
            return Optional.of("tvrdnja duža od " + MAX_CLAIM_LENGTH + " znakova");
        }
        if (reason.fieldsOrEmpty().isEmpty()) {
            return Optional.of("nema citiranih polja");
        }
        for (String field : reason.fieldsOrEmpty()) {
            if (!context.fieldPaths().contains(field)) {
                return Optional.of("polje nije u kontekstu: " + field);
            }
        }
        if (reason.sourcesOrEmpty().isEmpty()) {
            return Optional.of("nema izvora");
        }
        for (String source : reason.sourcesOrEmpty()) {
            if (!context.sourceUrls().contains(source)) {
                return Optional.of("izvor nije u kontekstu: " + source);
            }
        }
        List<String> forbidden = ForbiddenTerms.findIn(reason.claim());
        if (!forbidden.isEmpty()) {
            return Optional.of("zabranjena riječ: " + forbidden);
        }
        if (comparesWithPeersWithoutData(reason.claim(), context)) {
            return Optional.of("usporedba s općinama bez podataka o usporednoj skupini");
        }
        if (namesPerson(reason.claim(), context)) {
            return Optional.of("tvrdnja sadrži naziv obrta ili fizičke osobe");
        }
        return Optional.empty();
    }

    private static boolean comparesWithPeersWithoutData(String claim, AiContext context) {
        String lower = claim.toLowerCase(Locale.ROOT);
        return context.data().get(PEER_STATISTICS_PATH) == null
                && PEER_COMPARISON_STEMS.stream().anyMatch(lower::contains);
    }

    /**
     * A craft's name usually is the owner's name (docs/DATA_MODEL.md §8). Checked on the whole name and on each word of
     * it longer than three letters, ignoring case and diacritics, so a surname alone is caught too.
     */
    private static boolean namesPerson(String claim, AiContext context) {
        boolean isPerson = context.supplierType() == OrganizationType.CRAFT
                || context.supplierType() == OrganizationType.NATURAL_PERSON;
        if (!isPerson || isBlank(context.supplierName())) {
            return false;
        }
        String normalizedClaim = fold(claim);
        String normalizedName = fold(context.supplierName());
        if (normalizedClaim.contains(normalizedName)) {
            return true;
        }
        for (String word : normalizedName.split("[^\\p{L}]+")) {
            if (word.length() > 3 && !CRAFT_WORDS.contains(word) && normalizedClaim.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private static String fold(String text) {
        String lower = text.toLowerCase(Locale.ROOT).replace("đ", "d");
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private static AiValidation rejected(AiAnswer answer, AiLabel label, String reason) {
        return new AiValidation(AiAssessmentStatus.REJECTED, label,
                AiConfidence.fromCode(answer.confidence()).orElse(AiConfidence.LOW), answer.reasonsOrEmpty(),
                answer.missingDataOrEmpty(), answer.legitExplanationsOrEmpty(), reason);
    }
}
