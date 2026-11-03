package com.datathon.api.contract.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.datathon.api.indicator.dto.RuleCodes;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * Filters of the contract explorer. All are optional.
 *
 * @param severity  {@code vrijedi_provjeriti} or {@code informativno} (possible entry error in the source)
 * @param ruleCode  contracts with an active indicator of one of these rules (D1, D2, F, C); combined with severity and
 *                  band in the same indicator
 * @param band      magnitude band of that indicator: {@code nizak}, {@code srednji}, {@code visok}
 * @param synthetic include synthetic demo contracts; off by default (docs/UI_UX.md §4)
 * @param sort      {@code dateSigned} (default, newest first) or {@code value} (largest total first)
 */
public record ContractFilter(
        @Pattern(regexp = "\\d{11}", message = "OIB mora imati 11 znamenki") String buyerOib,
        @Pattern(regexp = "\\d{11}", message = "OIB mora imati 11 znamenki") String supplierOib,
        @Min(value = 2000, message = "godina nije ispravna") @Max(value = 2100, message = "godina nije ispravna") Integer year,
        Boolean hasIndicator,
        @Pattern(regexp = "vrijedi_provjeriti|informativno", message = "dopuštene vrijednosti su vrijedi_provjeriti i informativno")
        String severity,
        List<@Pattern(regexp = RuleCodes.PATTERN, message = RuleCodes.MESSAGE) String> ruleCode,
        @Pattern(regexp = "nizak|srednji|visok", message = "dopuštene vrijednosti su nizak, srednji i visok") String band,
        Boolean synthetic,
        @Pattern(regexp = "dateSigned|value", message = "dopuštene vrijednosti su dateSigned i value") String sort,
        Integer page,
        Integer size) {

    public boolean includesSynthetic() {
        return Boolean.TRUE.equals(synthetic);
    }

    public List<String> ruleCodes() {
        return ruleCode == null ? List.of() : ruleCode;
    }

    /** The same filter without the rule choice, for the per-rule counts next to each option. */
    public ContractFilter withoutRuleCodes() {
        return new ContractFilter(buyerOib, supplierOib, year, hasIndicator, severity, null, band, synthetic, sort, page, size);
    }
}
