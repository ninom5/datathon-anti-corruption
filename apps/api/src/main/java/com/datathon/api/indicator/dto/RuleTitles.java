package com.datathon.api.indicator.dto;

import com.datathon.api.rules.OverpaymentRule;
import com.datathon.api.rules.ValueIncreaseRule;

/** Croatian titles of the rules, as named in docs/ANOMALY_DETECTION.md. */
final class RuleTitles {

    private RuleTitles() {
    }

    static String of(String ruleCode) {
        return switch (ruleCode) {
            case ValueIncreaseRule.RULE_CODE -> "Povećanje vrijednosti ugovora";
            case OverpaymentRule.RULE_CODE -> "Isplaćeno više od ugovorenog";
            default -> ruleCode;
        };
    }
}
