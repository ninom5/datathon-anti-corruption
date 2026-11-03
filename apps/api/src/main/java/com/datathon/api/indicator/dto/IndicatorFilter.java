package com.datathon.api.indicator.dto;

import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * Filters of {@code GET /api/v1/indicators}. All are optional.
 *
 * @param ruleCode  one or more rule codes ({@code ruleCode=D_VALUE_INCREASE,F_SPLITTING} or repeated)
 * @param band      magnitude band within the rule: {@code nizak}, {@code srednji}, {@code visok}
 * @param synthetic include synthetic demo indicators; off by default
 */
public record IndicatorFilter(
        List<@Pattern(regexp = RuleCodes.PATTERN, message = RuleCodes.MESSAGE) String> ruleCode,
        @Pattern(regexp = "\\d{11}", message = "OIB mora imati 11 znamenki") String buyerOib,
        @Pattern(regexp = "nizak|srednji|visok", message = "dopuštene vrijednosti su nizak, srednji i visok") String band,
        @Pattern(regexp = "vrijedi_provjeriti|informativno", message = "dopuštene vrijednosti su vrijedi_provjeriti i informativno")
        String severity,
        Boolean synthetic,
        Integer page,
        Integer size) {

    public boolean includesSynthetic() {
        return Boolean.TRUE.equals(synthetic);
    }

    public List<String> ruleCodes() {
        return ruleCode == null ? List.of() : ruleCode;
    }
}
