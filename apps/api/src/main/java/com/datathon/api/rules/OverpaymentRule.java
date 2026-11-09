package com.datathon.api.rules;

import static com.datathon.api.rules.RuleSupport.eur;
import static com.datathon.api.rules.RuleSupport.percent;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.ingest.normalize.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverpaymentRule implements ContractRule {

    public static final String RULE_CODE = "D_OVERPAYMENT";

    static final String NOT_MEANING = "Razlika može nastati zbog ugovora s jediničnim cijenama, indeksacije cijena, "
            + "isplate koja uključuje druge troškove ili pogreške pri unosu u registar. Bez dokumentacije o isplatama "
            + "ne može se zaključiti je li isplata bila opravdana.";

    private final RuleThresholds thresholds;

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public Optional<IndicatorDraft> evaluate(Contract contract) {
        BigDecimal paid = contract.getPaidAmount();
        if (RuleSupport.isFrameworkContract(contract) || paid == null) {
            return Optional.empty();
        }
        boolean isGrossDerived = contract.getTotalValueGross() == null;
        BigDecimal gross = isGrossDerived ? grossWithoutAmendments(contract) : contract.getTotalValueGross();
        if (gross == null || gross.signum() <= 0) {
            return Optional.empty();
        }
        BigDecimal share = RuleSupport.growth(paid, gross);
        BigDecimal difference = paid.subtract(gross);
        if (share.compareTo(thresholds.ruleD().overpaymentShare()) <= 0
                || difference.compareTo(thresholds.ruleD().overpaymentMinEur()) < 0) {
            return Optional.empty();
        }
        return Optional.of(draft(contract, paid, gross, share, isGrossDerived));
    }

    private static BigDecimal grossWithoutAmendments(Contract contract) {
        BigDecimal amendments = contract.getAmendmentsValue();
        return amendments == null || amendments.signum() == 0 ? contract.getInitialValueGross() : null;
    }

    private IndicatorDraft draft(Contract contract, BigDecimal paid, BigDecimal gross, BigDecimal share, boolean isGrossDerived) {
        BigDecimal implausibleRatio = implausibleRatio(contract);
        boolean isImplausible = paid.compareTo(gross.multiply(implausibleRatio)) >= 0;
        boolean isUnitPrice = RuleSupport.isUnitPriceContract(contract);
        boolean isVatInconsistent = isGrossBelowNet(contract, gross);
        boolean isCurrencyMix = isKunaEuroRatio(paid, gross);
        DataQuality quality = isImplausible || isUnitPrice || isVatInconsistent || isCurrencyMix
                ? DataQuality.LOW : RuleSupport.baseQuality(contract, isGrossDerived);
        List<String> missing = new ArrayList<>(List.of("dokumentacija o isplatama"));
        if (contract.getPaymentNote() == null) {
            missing.add("obrazloženje razlike");
        }
        return new IndicatorDraft(RULE_CODE, share, "udio", "+" + percent(share) + " % iznad ugovorenog iznosa s PDV-om",
                quality, quality == DataQuality.LOW ? EvidenceStrength.WEAK : EvidenceStrength.MEDIUM,
                quality == DataQuality.LOW ? SeverityNote.INFORMATIVE : SeverityNote.WORTH_CHECKING,
                explanation(contract, paid, gross, share,
                        new Doubts(isImplausible, isUnitPrice, isVatInconsistent, isCurrencyMix), implausibleRatio),
                NOT_MEANING, missing,
                RuleSupport.evidence(contract, List.of("Ukupni iznos (sa izmjenama) s PDV-om", "Isplaćeni iznos",
                        "Obrazloženje iznosa plaćanja")));
    }

    private static boolean isGrossBelowNet(Contract contract, BigDecimal gross) {
        BigDecimal net = contract.getTotalValue() != null ? contract.getTotalValue() : contract.getInitialValue();
        return net != null && gross.compareTo(net) < 0;
    }

    private record Doubts(boolean isImplausible, boolean isUnitPrice, boolean isVatInconsistent, boolean isCurrencyMix) {
    }

    private boolean isKunaEuroRatio(BigDecimal paid, BigDecimal gross) {
        BigDecimal ratio = paid.divide(gross, 6, java.math.RoundingMode.HALF_UP);
        BigDecimal distance = ratio.divide(Money.HRK_PER_EUR, 6, java.math.RoundingMode.HALF_UP).subtract(BigDecimal.ONE).abs();
        return distance.compareTo(thresholds.ruleD().currencyMixTolerance()) <= 0;
    }

    private BigDecimal implausibleRatio(Contract contract) {
        boolean hasNote = contract.getPaymentNote() != null && !contract.getPaymentNote().isBlank();
        return hasNote ? thresholds.ruleD().implausibleRatio() : thresholds.ruleD().implausibleRatioWithoutNote();
    }

    private String explanation(Contract contract, BigDecimal paid, BigDecimal gross, BigDecimal share, Doubts doubts,
                               BigDecimal implausibleRatio) {
        StringBuilder text = new StringBuilder()
                .append("Isplaćeni iznos je ").append(eur(paid)).append(", a ukupni ugovoreni iznos s PDV-om ")
                .append(eur(gross)).append(". Razlika je ").append(eur(paid.subtract(gross)))
                .append(" (+").append(percent(share)).append(" %). ");
        text.append(contract.getPaymentNote() == null
                ? "Registar ne navodi obrazloženje razlike."
                : "Obrazloženje iz registra: \"" + contract.getPaymentNote() + "\".");
        if (doubts.isCurrencyMix()) {
            text.append(" Isplaćeni iznos je oko 7,53 puta veći od ugovorenog, što odgovara tečaju kune i eura. "
                    + "Vjerojatno je jedan od iznosa u registru upisan u kunama, a označen kao euro, pa se zapis "
                    + "prikazuje samo informativno.");
        } else if (doubts.isVatInconsistent()) {
            text.append(" U registru je iznos s PDV-om manji od iznosa bez PDV-a, što upućuje na pogrešku pri unosu, "
                    + "pa se zapis prikazuje samo informativno.");
        } else if (doubts.isImplausible()) {
            text.append(" Isplaćeni iznos je ").append(implausibleRatio.stripTrailingZeros().toPlainString())
                    .append(" ili više puta veći od ugovorenog").append(contract.getPaymentNote() == null ? ", a registar ne navodi obrazloženje" : "")
                    .append(". To najčešće upućuje na pogrešku pri unosu u registar "
                            + "(npr. izostavljen decimalni zarez) ili na ugovor s jediničnim cijenama, pa se zapis "
                            + "prikazuje samo informativno.");
        } else if (doubts.isUnitPrice()) {
            text.append(" Napomena u registru navodi jedinične cijene ili količine, pa ugovoreni iznos ne mora biti gornja granica.");
        }
        return text.toString();
    }
}
