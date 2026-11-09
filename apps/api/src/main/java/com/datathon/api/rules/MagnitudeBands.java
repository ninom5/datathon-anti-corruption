package com.datathon.api.rules;

import com.datathon.api.indicator.MagnitudeBand;
import com.datathon.api.rules.RuleThresholds.Band;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
            case YoungCompanyRule.RULE_CODE -> bands.youngCompany();
            default -> null;
        });
    }

    private static MagnitudeBand classify(Band band, BigDecimal magnitude) {
        if (reaches(magnitude, band.highFrom(), band)) {
            return MagnitudeBand.HIGH;
        }
        return reaches(magnitude, band.mediumFrom(), band) ? MagnitudeBand.MEDIUM : MagnitudeBand.LOW;
    }

    private static boolean reaches(BigDecimal value, BigDecimal bound, Band band) {
        int comparison = band.descending() ? bound.compareTo(value) : value.compareTo(bound);
        return band.strict() ? comparison > 0 : comparison >= 0;
    }
}
