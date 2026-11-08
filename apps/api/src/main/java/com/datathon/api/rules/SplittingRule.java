package com.datathon.api.rules;

import static com.datathon.api.rules.RuleSupport.date;
import static com.datathon.api.rules.RuleSupport.decimal;
import static com.datathon.api.rules.RuleSupport.eur;

import com.datathon.api.contract.Contract;
import com.datathon.api.contract.SubjectType;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.organization.Organization;
import com.datathon.api.rules.RuleThresholds.RuleF;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * F (docs/ANOMALY_DETECTION.md §F): two or more contracts of one buyer with the same supplier in the same CPV group,
 * each below the simple procurement threshold of its signing date, signed within 90 days and together at or above the
 * threshold. A window that spans a threshold change uses the lowest threshold in it. Details that docs/ leave open are
 * in work/decisions.md (2026-11-03, #41).
 */
@Component
@RequiredArgsConstructor
public class SplittingRule implements GroupRule {

    public static final String RULE_CODE = "F_SPLITTING";

    static final String NOT_MEANING = "Ugovori mogu odgovarati odvojenim, unaprijed nepredvidivim potrebama. Ovaj obrazac "
            + "sam po sebi ne dokazuje namjerno izbjegavanje postupka javne nabave.";

    private final RuleThresholds thresholds;

    /** A contract below the threshold of its own signing date. */
    record Candidate(Contract contract, BigDecimal value, BigDecimal threshold, SubjectType type, String cpv3, String cpv2) {
    }

    private record Window(List<Candidate> members, BigDecimal threshold, BigDecimal sum, boolean isCpv2Match) {
    }

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public List<GroupFinding> evaluate(BuyerContracts input) {
        List<Candidate> candidates = belowThreshold(input.contracts());
        List<Window> windows = new ArrayList<>();
        for (List<Candidate> group : groupBy(candidates, candidate -> supplierKey(candidate) + "|" + candidate.cpv3())) {
            windows.addAll(windowsOf(group, false));
        }
        // Weaker match: different CPV3 within one CPV2, only for contracts not already in a CPV3 window.
        Set<Contract> used = new HashSet<>();
        windows.forEach(window -> window.members().forEach(member -> used.add(member.contract())));
        List<Candidate> rest = candidates.stream().filter(candidate -> !used.contains(candidate.contract())).toList();
        for (List<Candidate> group : groupBy(rest, candidate -> supplierKey(candidate) + "|" + candidate.cpv2())) {
            windows.addAll(windowsOf(group, true));
        }
        return windows.stream().map(window -> finding(input, window)).toList();
    }

    private List<Candidate> belowThreshold(List<Contract> contracts) {
        List<Candidate> candidates = new ArrayList<>();
        for (Contract contract : contracts) {
            BigDecimal value = contract.getInitialValue();
            String cpv3 = RuleSupport.cpvPrefix(contract, 3);
            if (contract.getSupplier() == null || contract.getDateSigned() == null || cpv3 == null || value == null
                    || value.signum() <= 0 || RuleSupport.isFrameworkContract(contract) || isPublicProcedure(contract)) {
                continue;
            }
            SubjectType type = RuleSupport.thresholdType(contract);
            Optional<BigDecimal> limit = thresholds.simpleProcurementOn(contract.getDateSigned()).map(period -> period.limit(type));
            if (limit.isPresent() && value.compareTo(limit.get()) < 0) {
                candidates.add(new Candidate(contract, value, limit.get(), type, cpv3, cpv3.substring(0, 2)));
            }
        }
        return candidates;
    }

    /** A contract from a ZJN procedure (e.g. a lot of an open procedure) went through public procurement already. */
    private boolean isPublicProcedure(Contract contract) {
        String procedure = contract.getProcedureType();
        if (procedure == null) {
            return false;
        }
        String lower = procedure.toLowerCase(java.util.Locale.ROOT);
        return thresholds.ruleF().publicProcedureMarkers().stream().anyMatch(lower::contains);
    }

    private static String supplierKey(Candidate candidate) {
        return String.valueOf(candidate.contract().getSupplier().getId());
    }

    private static Collection<List<Candidate>> groupBy(List<Candidate> candidates, Function<Candidate, String> key) {
        Map<String, List<Candidate>> groups = candidates.stream()
                .collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.toList()));
        groups.values().forEach(group -> group.sort(Comparator.comparing((Candidate candidate) -> candidate.contract().getDateSigned())
                .thenComparing(candidate -> candidate.contract().getId())));
        return groups.values();
    }

    /**
     * Each start date opens the longest window of at most {@code window-days}; a qualifying window is reported and the
     * search continues after its last contract, so the same contracts never give two indicators.
     */
    private List<Window> windowsOf(List<Candidate> group, boolean isCpv2Match) {
        RuleF rule = thresholds.ruleF();
        List<Window> found = new ArrayList<>();
        int start = 0;
        while (start < group.size()) {
            int end = start;
            while (end + 1 < group.size() && daysBetween(group.get(start), group.get(end + 1)) <= rule.windowDays()) {
                end++;
            }
            Optional<Window> window = qualify(group.subList(start, end + 1), isCpv2Match);
            if (window.isPresent()) {
                found.add(window.get());
                start = end + 1;
            } else {
                start++;
            }
        }
        return found;
    }

    private Optional<Window> qualify(List<Candidate> members, boolean isCpv2Match) {
        if (members.size() < 2 || (isCpv2Match && members.stream().map(Candidate::cpv3).distinct().count() < 2)) {
            return Optional.empty();
        }
        BigDecimal threshold = members.stream().map(Candidate::threshold).min(Comparator.naturalOrder()).orElseThrow();
        BigDecimal sum = members.stream().map(Candidate::value).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal largest = members.stream().map(Candidate::value).max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal minLargest = threshold.multiply(thresholds.ruleF().nearBand()).multiply(thresholds.ruleF().maxShareFactor());
        if (sum.compareTo(threshold) < 0 || largest.compareTo(minLargest) < 0) {
            return Optional.empty();
        }
        return Optional.of(new Window(List.copyOf(members), threshold, sum, isCpv2Match));
    }

    private static long daysBetween(Candidate first, Candidate last) {
        return ChronoUnit.DAYS.between(first.contract().getDateSigned(), last.contract().getDateSigned());
    }

    private GroupFinding finding(BuyerContracts input, Window window) {
        List<Contract> contracts = window.members().stream().map(Candidate::contract).toList();
        Organization supplier = contracts.getFirst().getSupplier();
        String cpv = window.isCpv2Match() ? window.members().getFirst().cpv2() : window.members().getFirst().cpv3();
        BigDecimal ratio = window.sum().divide(window.threshold(), 4, RoundingMode.HALF_UP);
        boolean isOwnCompany = input.isFoundedByBuyer(supplier);
        boolean isRecurring = thresholds.ruleF().recurringCpv2().contains(window.members().getFirst().cpv2());
        boolean hasDerivedType = contracts.stream().anyMatch(RuleSupport::isSubjectTypeDerived);
        DataQuality quality = RuleSupport.groupQuality(contracts, hasDerivedType);
        List<String> missing = new ArrayList<>(List.of("plan nabave", "opis pojedinačnih potreba"));
        if (hasDerivedType) {
            missing.add("vrsta predmeta (izvedena iz CPV-a)");
        }
        IndicatorDraft draft = new IndicatorDraft(RULE_CODE, ratio, "omjer",
                "zbroj je " + decimal(ratio) + " × prag jednostavne nabave", quality, strength(window),
                isOwnCompany || isRecurring ? SeverityNote.INFORMATIVE : SeverityNote.WORTH_CHECKING,
                explanation(input, window, supplier, cpv, ratio, isOwnCompany, isRecurring), NOT_MEANING, missing,
                RuleSupport.groupEvidence(contracts, "Iznos bez PDV-a"));
        return new GroupFinding(draft, Indicator.SUBJECT_GROUP, subjectRef(input, window, supplier, cpv), contracts);
    }

    /** docs §F: "jaka" with >= 3 contracts each >= 70 % of the threshold, "slaba" when only the CPV division matches. */
    private EvidenceStrength strength(Window window) {
        if (window.isCpv2Match()) {
            return EvidenceStrength.WEAK;
        }
        BigDecimal near = window.threshold().multiply(thresholds.ruleF().nearBand());
        boolean isStrong = window.members().size() >= thresholds.ruleF().strongMinContracts()
                && window.members().stream().allMatch(member -> member.value().compareTo(near) >= 0);
        return isStrong ? EvidenceStrength.STRONG : EvidenceStrength.MEDIUM;
    }

    private String explanation(BuyerContracts input, Window window, Organization supplier, String cpv, BigDecimal ratio,
                               boolean isOwnCompany, boolean isRecurring) {
        List<Candidate> members = window.members();
        boolean isWorks = members.stream().allMatch(member -> member.type() == SubjectType.WORKS);
        StringBuilder text = new StringBuilder()
                .append(periodLabel(members)).append(" naručitelj ").append(input.buyer().getName())
                .append(" sklopio je ").append(members.size()).append(" ugovora s istim dobavljačem, ")
                .append(RuleSupport.supplierName(supplier)).append(",");
        if (window.isCpv2Match()) {
            text.append(" u istoj CPV skupini na razini dvije znamenke (").append(cpv).append("), ali u različitim podskupinama (")
                    .append(String.join(", ", new TreeSet<>(members.stream().map(Candidate::cpv3).toList())))
                    .append("), pa je podudaranje predmeta slabije");
        } else {
            text.append(" u istoj CPV skupini (").append(cpv).append(')');
        }
        text.append(". Svaki je ispod praga jednostavne nabave ").append(isWorks ? "za radove" : "za robe i usluge")
                .append(" koji je vrijedio na dan sklapanja. Ukupno vrijede ").append(eur(window.sum()))
                .append(" bez PDV-a, što je ").append(decimal(ratio)).append(" × prag od ").append(eur(window.threshold())).append('.');
        if (isThresholdChange(members)) {
            text.append(" Razdoblje obuhvaća promjenu praga, pa se zbroj uspoređuje s nižim pragom.");
        }
        if (isOwnCompany) {
            text.append(" Prema registru javnopravnih tijela (JPT) naručitelj je osnivač dobavljača, pa je ponavljanje "
                    + "ugovora s vlastitim društvom očekivano i prikazuje se samo informativno.");
        }
        if (isRecurring) {
            text.append(" CPV skupina ").append(members.getFirst().cpv2()).append(" obuhvaća ponavljajuće nabave (gorivo, "
                    + "energija ili komunalne usluge), pa se obrazac prikazuje samo informativno.");
        }
        return text.toString();
    }

    /** "Između 03.02.2025. i 28.04.2025." or, when all contracts were signed on one day, "Dana 28.08.2024.". */
    private static String periodLabel(List<Candidate> members) {
        var first = members.getFirst().contract().getDateSigned();
        var last = members.getLast().contract().getDateSigned();
        return first.equals(last) ? "Dana " + date(first) : "Između " + date(first) + " i " + date(last);
    }

    private static Map<String, Object> subjectRef(BuyerContracts input, Window window, Organization supplier, String cpv) {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("buyerOib", input.buyer().getOib());
        ref.put("supplierOib", supplier.getOib());
        ref.put("cpv", cpv);
        ref.put("cpvLevel", cpv.length());
        ref.put("from", window.members().getFirst().contract().getDateSigned().toString());
        ref.put("to", window.members().getLast().contract().getDateSigned().toString());
        // The lowest threshold in the window, the one the sum is compared with; the UI draws it, never recomputes it.
        ref.put("threshold", window.threshold());
        ref.put("contractIds", window.members().stream().map(member -> member.contract().getId()).toList());
        return ref;
    }

    /**
     * 200.000 kn converted at 7,53450 is 26.544,56 EUR and the 2023 threshold is 26.540 EUR: the same legal threshold,
     * so a window over 1.1.2023 is not a threshold change. Only a difference of at least 1 EUR counts.
     */
    private static boolean isThresholdChange(List<Candidate> members) {
        BigDecimal min = members.stream().map(Candidate::threshold).min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = members.stream().map(Candidate::threshold).max(BigDecimal::compareTo).orElseThrow();
        return max.subtract(min).compareTo(BigDecimal.ONE) >= 0;
    }
}
