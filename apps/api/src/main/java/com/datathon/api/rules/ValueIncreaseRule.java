package com.datathon.api.rules;

import static com.datathon.api.rules.RuleSupport.eur;
import static com.datathon.api.rules.RuleSupport.percent;

import com.datathon.api.contract.Contract;
import com.datathon.api.contract.SubjectType;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.rules.RuleThresholds.ModificationLimits;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * D1 (docs/ANOMALY_DETECTION.md §D): the total value with amendments grew above the ZJN de minimis share
 * (10 % goods/services, 15 % works) of a contract worth at least 10.000 EUR. Framework agreements are excluded.
 */
@Component
@RequiredArgsConstructor
public class ValueIncreaseRule implements ContractRule {

    public static final String RULE_CODE = "D_VALUE_INCREASE";

    static final String NOT_MEANING = "Povećanje može biti predviđeno ugovorom ili uzrokovano okolnostima koje naručitelj "
            + "nije mogao predvidjeti. Bez dokumentacije o izmjeni ne može se zaključiti je li izmjena bila opravdana.";

    private final RuleThresholds thresholds;

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public Optional<IndicatorDraft> evaluate(Contract contract) {
        BigDecimal initial = contract.getInitialValue();
        if (RuleSupport.isFrameworkContract(contract) || initial == null || initial.signum() <= 0
                || initial.compareTo(thresholds.ruleD().minInitialValueEur()) < 0) {
            return Optional.empty();
        }
        boolean isTotalDerived = contract.getTotalValue() == null;
        BigDecimal total = isTotalDerived ? sumWithAmendments(contract) : contract.getTotalValue();
        Optional<ModificationLimits> limits = thresholds.modificationLimitsOn(contract.getDateSigned());
        if (total == null || limits.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal growth = RuleSupport.growth(total, initial);
        if (growth.compareTo(limits.get().deMinimis(contract.getSubjectType())) <= 0) {
            return Optional.empty();
        }
        return Optional.of(draft(contract, initial, total, growth, limits.get(), isTotalDerived));
    }

    private static BigDecimal sumWithAmendments(Contract contract) {
        return contract.getAmendmentsValue() == null ? null : contract.getInitialValue().add(contract.getAmendmentsValue());
    }

    private IndicatorDraft draft(Contract contract, BigDecimal initial, BigDecimal total, BigDecimal growth,
                                 ModificationLimits limits, boolean isTotalDerived) {
        boolean isImplausible = total.compareTo(initial.multiply(thresholds.ruleD().implausibleRatio())) >= 0;
        boolean isUnitPrice = RuleSupport.isUnitPriceContract(contract);
        boolean hasDerivedInput = isTotalDerived || contract.getSubjectType() == null;
        DataQuality quality = isImplausible || isUnitPrice ? DataQuality.LOW : RuleSupport.baseQuality(contract, hasDerivedInput);
        List<String> missing = new ArrayList<>(List.of("razlog izmjene", "datum izmjene"));
        if (contract.getSubjectType() == null) {
            missing.add("vrsta predmeta");
        }
        return new IndicatorDraft(RULE_CODE, growth, "udio", "+" + percent(growth) + " % u odnosu na početni iznos",
                quality, quality == DataQuality.LOW ? EvidenceStrength.WEAK : EvidenceStrength.MEDIUM,
                quality == DataQuality.LOW ? SeverityNote.INFORMATIVE : SeverityNote.WORTH_CHECKING,
                explanation(contract, initial, total, growth, limits, isImplausible, isUnitPrice), NOT_MEANING, missing,
                RuleSupport.evidence(contract, List.of("Iznos bez PDV-a", "Iznos izmjene bez PDV-a",
                        "Ukupni iznos (sa izmjenama) bez PDV-a")));
    }

    private String explanation(Contract contract, BigDecimal initial, BigDecimal total, BigDecimal growth,
                               ModificationLimits limits, boolean isImplausible, boolean isUnitPrice) {
        String kind = contract.getSubjectType() == SubjectType.WORKS ? "za radove" : "za robe i usluge";
        StringBuilder text = new StringBuilder()
                .append("Ugovor je sklopljen na ").append(eur(initial)).append(" bez PDV-a, a ukupni iznos s izmjenama je ")
                .append(eur(total)).append(" (+").append(percent(growth)).append(" %). Registar ne navodi razlog izmjene. ")
                .append("Za izmjene male vrijednosti ZJN predviđa granicu od ")
                .append(percent(limits.deMinimis(contract.getSubjectType()))).append(" % ").append(kind)
                .append(" (čl. 320.), za dodatne radove, usluge i robe ").append(percent(limits.additionalSupplies()))
                .append(" % (čl. 316. st. 2), a za nepredviđene okolnosti ").append(percent(limits.unforeseenCircumstances()))
                .append(" % (čl. 317.). Izmjena predviđena u dokumentaciji (čl. 315.) nema takvo ograničenje.");
        if (isImplausible) {
            text.append(" Ukupni iznos je ").append(thresholds.ruleD().implausibleRatio().stripTrailingZeros().toPlainString())
                    .append(" ili više puta veći od početnog. To najčešće upućuje na pogrešku pri unosu u registar "
                            + "(npr. izostavljen decimalni zarez), pa se zapis prikazuje samo informativno.");
        }
        if (isUnitPrice) {
            text.append(" Napomena u registru navodi jedinične cijene ili količine, pa ugovoreni iznos ne mora biti gornja granica.");
        }
        return text.toString();
    }
}
