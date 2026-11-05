package com.datathon.api.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.organization.OrganizationType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/** Every case of docs/ANOMALY_DETECTION.md §4.4, plus the stricter additions noted in AiAssessmentValidator. */
class AiAssessmentValidatorTest {

    private static final String ROW_URL = "https://primjer.invalid/registar.xlsx#sheet=1;row=12";
    private static final String CRAFT_NAME = "Obrt Primjerić, vl. Ivo Primjerić";

    private static AiContext context(OrganizationType supplierType, String supplierName) {
        Map<String, Object> contract = new TreeMap<>();
        contract.put("predmet", "Usluge savjetovanja");
        contract.put("vrijednost_eur", 24900);
        contract.put("izvor_url", ROW_URL);
        Map<String, Object> supplier = new TreeMap<>();
        supplier.put("naziv", supplierName);
        supplier.put("nkd", null);
        Map<String, Object> rule = new TreeMap<>();
        rule.put("rule_code", "F_SPLITTING");
        rule.put("izvori", List.of(ROW_URL));
        Map<String, Object> data = new TreeMap<>();
        data.put("ugovor", contract);
        data.put("dobavljac", supplier);
        data.put("pravila", List.of(rule));
        return new AiContext(1L, data, "{}", ContextPaths.fieldPaths(data), ContextPaths.sourceUrls(data),
                supplierType, supplierName);
    }

    private static AiContext companyContext() {
        return context(OrganizationType.LLC, "Primjer d.o.o.");
    }

    private static AiAnswer.Reason reason(String claim, String... fields) {
        return new AiAnswer.Reason(claim, Arrays.asList(fields), List.of(ROW_URL));
    }

    private static AiAnswer answer(String label, List<AiAnswer.Reason> reasons, List<String> explanations) {
        return new AiAnswer(label, reasons, List.of("broj ponuda"), explanations, "srednja");
    }

    private static final List<String> EXPLANATION = List.of("Potreba se nije mogla predvidjeti u planu nabave.");

    @Test
    void groundedAnswerIsAccepted() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Predmet je generički za iznos od 24.900 €.", "ugovor.predmet", "ugovor.vrijednost_eur")),
                EXPLANATION), companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.ACCEPTED);
        assertThat(validation.label()).isEqualTo(AiLabel.WORTH_CHECKING);
        assertThat(validation.reasons()).hasSize(1);
        assertThat(validation.rejectionReason()).isNull();
    }

    @Test
    void ruleFieldsAreCitableWithAndWithoutIndex() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Pravilo F je aktivno.", "pravila[0].rule_code", "pravila.rule_code")), EXPLANATION),
                companyContext());

        assertThat(validation.isAccepted()).isTrue();
    }

    @Test
    void inventedFieldRejectsTheReason() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Dobavljač ima malo zaposlenih.", "dobavljac.broj_zaposlenih")), EXPLANATION),
                companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(validation.label()).isEqualTo(AiLabel.NO_FLAG);
        assertThat(validation.rejectionReason()).contains("dobavljac.broj_zaposlenih");
    }

    @Test
    void nullFieldIsCitableToSayItIsNotPublished() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Djelatnost dobavljača nije poznata.", "dobavljac.nkd", "ugovor.predmet")), EXPLANATION),
                companyContext());

        assertThat(validation.isAccepted()).isTrue();
    }

    @Test
    void foreignUrlRejectsTheReason() {
        AiAnswer.Reason foreign = new AiAnswer.Reason("Predmet je generički.", List.of("ugovor.predmet"),
                List.of("https://drugi-izvor.invalid/clanak"));
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti", List.of(foreign),
                EXPLANATION), companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(validation.rejectionReason()).contains("izvor nije u kontekstu");
    }

    @Test
    void reasonWithoutSourceIsRejected() {
        AiAnswer.Reason noSource = new AiAnswer.Reason("Predmet je generički.", List.of("ugovor.predmet"), List.of());
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti", List.of(noSource),
                EXPLANATION), companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
    }

    @Test
    void forbiddenWordRejectsTheReason() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Moguća je korupcija pri dodjeli.", "ugovor.predmet")), EXPLANATION), companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(validation.rejectionReason()).contains("korupcij");
    }

    @Test
    void forbiddenWordInExplanationRejectsWholeAnswer() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Predmet je generički.", "ugovor.predmet")),
                List.of("Ne mora biti nezakonito.")), companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
    }

    @Test
    void craftNameInClaimIsRejectedAlsoBySurnameAlone() {
        AiContext craft = context(OrganizationType.CRAFT, CRAFT_NAME);
        AiValidation byName = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Ugovor je dobio " + CRAFT_NAME + ".", "ugovor.predmet")), EXPLANATION), craft);
        AiValidation bySurname = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Dobavljač Primjeric nema reference.", "ugovor.predmet")), EXPLANATION), craft);
        AiValidation neutral = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Dobavljač je dobio generički predmet.", "ugovor.predmet")), EXPLANATION), craft);

        assertThat(byName.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(bySurname.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(neutral.status()).isEqualTo(AiAssessmentStatus.ACCEPTED);
    }

    @Test
    void companyNameInClaimIsAllowed() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Primjer d.o.o. dobio je generički predmet.", "ugovor.predmet", "dobavljac.naziv")),
                EXPLANATION), companyContext());

        assertThat(validation.isAccepted()).isTrue();
    }

    @Test
    void emptyLegitExplanationsRejectWholeAnswer() {
        AiValidation empty = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Predmet je generički.", "ugovor.predmet")), List.of()), companyContext());
        AiValidation blank = AiAssessmentValidator.validate(answer("nema_oznake", List.of(), List.of(" ")),
                companyContext());

        assertThat(empty.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(empty.rejectionReason()).contains("legitimnog");
        assertThat(blank.status()).isEqualTo(AiAssessmentStatus.REJECTED);
    }

    @Test
    void flagWithoutReasonsIsRejected() {
        AiValidation validation = AiAssessmentValidator.validate(answer("visoka_pozornost", List.of(), EXPLANATION),
                companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(validation.rejectionReason()).contains("bez razloga");
    }

    @Test
    void noFlagWithoutReasonsIsAccepted() {
        AiValidation validation = AiAssessmentValidator.validate(answer("nema_oznake", List.of(), EXPLANATION),
                companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.ACCEPTED);
        assertThat(validation.label()).isEqualTo(AiLabel.NO_FLAG);
    }

    @Test
    void oneBadReasonOfTwoIsDroppedAndHighAttentionNeedsTwoAcceptedReasons() {
        List<AiAnswer.Reason> reasons = new ArrayList<>();
        reasons.add(reason("Predmet je generički.", "ugovor.predmet"));
        reasons.add(reason("Dobavljač je mlad.", "dobavljac.datum_osnivanja"));
        AiValidation validation = AiAssessmentValidator.validate(answer("visoka_pozornost", reasons, EXPLANATION),
                companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.ACCEPTED);
        assertThat(validation.reasons()).extracting(AiAnswer.Reason::claim).containsExactly("Predmet je generički.");
        assertThat(validation.label()).isEqualTo(AiLabel.WORTH_CHECKING);
        assertThat(validation.rejectionReason()).contains("razlog 2").contains("spušteno");
    }

    @Test
    void peerComparisonWithoutPeerStatisticsIsRejected() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("Iznos odstupa od usporednih općina.", "ugovor.vrijednost_eur")), EXPLANATION),
                companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(validation.rejectionReason()).contains("usporedb");
    }

    @Test
    void tooLongClaimIsRejected() {
        AiValidation validation = AiAssessmentValidator.validate(answer("vrijedi_provjeriti",
                List.of(reason("a".repeat(AiAssessmentValidator.MAX_CLAIM_LENGTH + 1), "ugovor.predmet")),
                EXPLANATION), companyContext());

        assertThat(validation.status()).isEqualTo(AiAssessmentStatus.REJECTED);
    }
}
