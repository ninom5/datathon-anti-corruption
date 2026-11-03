package com.datathon.api.rules;

import com.datathon.api.indicator.MagnitudeBand;
import com.datathon.api.rules.RuleThresholds.Band;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Band of a magnitude within its rule, from thresholds.yaml "bands" (a heuristic for filters, not a score). */
@Component
@RequiredArgsConstructor
public class MagnitudeBands {

    private final RuleThresholds thresholds;

    public Optional<MagnitudeBand> of(String ruleCode, BigDecimal magnitude) {
        if (magnitude == null) {
            return Optional.empty();
        }
        return boundsOf(ruleCode).map(band -> classify(band, magnitude));
    }

    private Optional<Band> boundsOf(String ruleCode) {
        RuleThresholds.Bands bands = thresholds.bands();
        return Optional.ofNullable(switch (ruleCode) {
            case ValueIncreaseRule.RULE_CODE -> bands.valueIncrease();
            case OverpaymentRule.RULE_CODE -> bands.overpayment();
            case SplittingRule.RULE_CODE -> bands.splitting();
            case ConcentrationRule.RULE_CODE -> bands.concentration();
            default -> null;
        });
    }

    private static MagnitudeBand classify(Band band, BigDecimal magnitude) {
        if (reaches(magnitude, band.highFrom(), band.strict())) {
            return MagnitudeBand.HIGH;
        }
        return reaches(magnitude, band.mediumFrom(), band.strict()) ? MagnitudeBand.MEDIUM : MagnitudeBand.LOW;
    }

    private static boolean reaches(BigDecimal value, BigDecimal bound, boolean isStrict) {
        int comparison = value.compareTo(bound);
        return isStrict ? comparison > 0 : comparison >= 0;
    }
}
