package com.datathon.api.indicator.dto;

import com.datathon.api.rules.ConcentrationRule;
import com.datathon.api.rules.OverpaymentRule;
import com.datathon.api.rules.SplittingRule;
import com.datathon.api.rules.ValueIncreaseRule;

/** Croatian titles of the rules, as named in docs/ANOMALY_DETECTION.md. */
public final class RuleTitles {

    private RuleTitles() {
    }

    public static String of(String ruleCode) {
        return switch (ruleCode) {
            case ValueIncreaseRule.RULE_CODE -> "Povećanje vrijednosti ugovora";
            case OverpaymentRule.RULE_CODE -> "Isplaćeno više od ugovorenog";
            case SplittingRule.RULE_CODE -> "Više manjih ugovora s istim dobavljačem (moguće cjepkanje)";
            case ConcentrationRule.RULE_CODE -> "Ponovljene dodjele istom dobavljaču (koncentracija)";
            default -> ruleCode;
        };
    }
}
