package com.datathon.api.rules;

import static com.datathon.api.rules.RuleSupport.decimal;
import static com.datathon.api.rules.RuleSupport.eur;
import static com.datathon.api.rules.RuleSupport.percent;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.organization.Organization;
import com.datathon.api.rules.RuleThresholds.RuleC;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * C (docs/ANOMALY_DETECTION.md §C): in one buyer's CPV division over its latest 3 years with contracts, one supplier
 * holds at least 70 % of the value and HHI is at least 5.000. Only cells with enough contracts, value and years are
 * eligible, because tiny cells are concentrated by construction. The top supplier needs at least two contracts in the
 * cell: one large contract alone is not a repeated award (work/decisions.md, #41). The buyer's own company (JPT founders) is informative.
 */
@Component
@RequiredArgsConstructor
public class ConcentrationRule implements GroupRule {

    public static final String RULE_CODE = "C_CONCENTRATION";

    static final String NOT_MEANING = "Na malom tržištu često postoji samo jedan ili dva izvođača. Koncentracija ne govori "
            + "je li postupak bio pošten.";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final RuleThresholds thresholds;

    private record Cell(String cpv2, List<Contract> contracts, BigDecimal total) {
    }

    private record Shares(Organization top, BigDecimal topValue, long topContracts, BigDecimal topShare, BigDecimal hhi) {
    }

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public List<GroupFinding> evaluate(BuyerContracts input) {
        List<Contract> valued = input.contracts().stream()
                .filter(contract -> contract.getSupplier() != null && contract.getDateSigned() != null
                        && RuleSupport.cpvPrefix(contract, 2) != null && isPositive(RuleSupport.valueOf(contract))
                        && !RuleSupport.isCallOffRecord(contract))
                .toList();
        Set<Integer> period = latestYears(valued);
        Map<String, List<Contract>> byCpv2 = valued.stream()
                .filter(contract -> period.contains(contract.getDateSigned().getYear()))
                .collect(Collectors.groupingBy(contract -> RuleSupport.cpvPrefix(contract, 2), TreeMap::new, Collectors.toList()));
        List<GroupFinding> findings = new ArrayList<>();
        byCpv2.forEach((cpv2, contracts) -> eligible(cpv2, contracts)
                .ifPresent(cell -> flagged(cell).ifPresent(shares -> findings.add(finding(input, cell, shares, period)))));
        return findings;
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    /** docs §C "zadnje 3 godine ili sve dostupne": the buyer's latest calendar years that have contracts. */
    private Set<Integer> latestYears(List<Contract> contracts) {
        return contracts.stream().map(contract -> contract.getDateSigned().getYear())
                .collect(Collectors.toCollection(TreeSet::new)).descendingSet().stream()
                .limit(thresholds.ruleC().periodYears())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Optional<Cell> eligible(String cpv2, List<Contract> contracts) {
        RuleC rule = thresholds.ruleC();
        BigDecimal total = contracts.stream().map(RuleSupport::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
        long years = contracts.stream().map(contract -> contract.getDateSigned().getYear()).distinct().count();
        boolean isEligible = contracts.size() >= rule.minContracts() && total.compareTo(rule.minTotalEur()) >= 0
                && years >= rule.minDistinctYears();
        return isEligible ? Optional.of(new Cell(cpv2, contracts, total)) : Optional.empty();
    }

    /** s_i = V_i / ΣV, HHI = Σ (100 · s_i)², top1 = max s_i (docs §C). */
    private Optional<Shares> flagged(Cell cell) {
        Map<Organization, List<Contract>> bySupplier = cell.contracts().stream()
                .collect(Collectors.groupingBy(Contract::getSupplier, LinkedHashMap::new, Collectors.toList()));
        BigDecimal hhi = BigDecimal.ZERO;
        Map.Entry<Organization, BigDecimal> top = null;
        for (Map.Entry<Organization, List<Contract>> entry : bySupplier.entrySet()) {
            BigDecimal value = entry.getValue().stream().map(RuleSupport::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal share = value.divide(cell.total(), 8, RoundingMode.HALF_UP);
            hhi = hhi.add(share.multiply(HUNDRED).pow(2));
            if (top == null || value.compareTo(top.getValue()) > 0) {
                top = Map.entry(entry.getKey(), value);
            }
        }
        BigDecimal topShare = top.getValue().divide(cell.total(), 6, RoundingMode.HALF_UP);
        hhi = hhi.setScale(0, RoundingMode.HALF_UP);
        long topContracts = bySupplier.get(top.getKey()).size();
        if (topShare.compareTo(thresholds.ruleC().top1Share()) < 0 || hhi.compareTo(thresholds.ruleC().hhi()) < 0
                || topContracts < thresholds.ruleC().minTopContracts()) {
            return Optional.empty();
        }
        return Optional.of(new Shares(top.getKey(), top.getValue(), topContracts, topShare, hhi));
    }

    private GroupFinding finding(BuyerContracts input, Cell cell, Shares shares, Set<Integer> period) {
        List<Contract> contracts = cell.contracts().stream()
                .sorted(Comparator.comparing(Contract::getDateSigned).thenComparing(Contract::getId)).toList();
        boolean isOwnCompany = input.isFoundedByBuyer(shares.top());
        boolean isNaturalMonopoly = thresholds.ruleC().naturalMonopolyCpv2().contains(cell.cpv2());
        EvidenceStrength strength = contracts.size() >= thresholds.ruleC().strongMinContracts()
                ? EvidenceStrength.STRONG : EvidenceStrength.MEDIUM;
        IndicatorDraft draft = new IndicatorDraft(RULE_CODE, shares.hhi(), "HHI",
                "HHI " + decimal(shares.hhi()) + "; najveći dobavljač " + percent(shares.topShare()) + " % vrijednosti",
                RuleSupport.groupQuality(contracts, false), strength,
                isOwnCompany ? SeverityNote.INFORMATIVE : SeverityNote.WORTH_CHECKING,
                explanation(input, cell, shares, period, isOwnCompany, isNaturalMonopoly), NOT_MEANING,
                List.of("broj ponuda", "tržište mogućih dobavljača"),
                RuleSupport.groupEvidence(contracts, "Ukupni iznos (sa izmjenama) bez PDV-a"));
        // Only the top supplier's contracts carry the indicator; the other suppliers' contracts stay in the evidence
        // and in subject_ref, so the shares can be checked, but they are not marked "koncentracija" in the explorer.
        List<Contract> topSupplierContracts = contracts.stream()
                .filter(contract -> contract.getSupplier() == shares.top())
                .toList();
        return new GroupFinding(draft, Indicator.SUBJECT_BUYER_CPV, subjectRef(input, cell, shares, period, contracts),
                topSupplierContracts);
    }

    private static String explanation(BuyerContracts input, Cell cell, Shares shares, Set<Integer> period,
                                      boolean isOwnCompany, boolean isNaturalMonopoly) {
        StringBuilder text = new StringBuilder()
                .append("U razdoblju ").append(periodLabel(period)).append(" naručitelj ").append(input.buyer().getName())
                .append(" sklopio je ").append(cell.contracts().size()).append(" ugovora u CPV skupini ").append(cell.cpv2())
                .append(" vrijednih ukupno ").append(eur(cell.total())).append(" bez PDV-a. Dobavljač ")
                .append(RuleSupport.supplierName(shares.top())).append(" dobio je ").append(shares.topContracts()).append(" od ")
                .append(cell.contracts().size()).append(" ugovora (").append(percent(shares.topShare()))
                .append(" % vrijednosti, ").append(eur(shares.topValue())).append("; HHI ").append(decimal(shares.hhi())).append(").")
                .append(" Broj ponuda nije javno dostupan, pa se ne zna koliko se dobavljača natjecalo.");
        if (isNaturalMonopoly) {
            text.append(" CPV skupina ").append(cell.cpv2()).append(" obuhvaća djelatnosti u kojima često postoji samo jedan "
                    + "mogući dobavljač (mogući prirodni monopol).");
        }
        if (isOwnCompany) {
            text.append(" Prema registru javnopravnih tijela (JPT) naručitelj je osnivač ovog dobavljača (npr. komunalno "
                    + "društvo), pa je koncentracija očekivana i prikazuje se samo informativno.");
        }
        return text.toString();
    }

    /** "2023.–2025." for consecutive years; "2021., 2024. i 2025." when the buyer has years without contracts. */
    private static String periodLabel(Set<Integer> period) {
        TreeSet<Integer> years = new TreeSet<>(period);
        if (years.size() == 1) {
            return years.first() + ".";
        }
        if (years.last() - years.first() == years.size() - 1) {
            return years.first() + ".–" + years.last() + ".";
        }
        List<String> labels = years.stream().map(year -> year + ".").toList();
        return String.join(", ", labels.subList(0, labels.size() - 1)) + " i " + labels.getLast();
    }

    private static Map<String, Object> subjectRef(BuyerContracts input, Cell cell, Shares shares, Set<Integer> period,
                                                  List<Contract> contracts) {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("buyerOib", input.buyer().getOib());
        ref.put("cpv2", cell.cpv2());
        ref.put("years", List.copyOf(new TreeSet<>(period)));
        ref.put("topSupplierOib", shares.top().getOib());
        ref.put("contractIds", contracts.stream().map(Contract::getId).toList());
        return ref;
    }
}
