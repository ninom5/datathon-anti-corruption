package com.datathon.api.contract.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.datathon.api.indicator.dto.RuleCodes;
import jakarta.validation.constraints.Pattern;
import java.util.List;

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

    public ContractFilter withoutRuleCodes() {
        return new ContractFilter(buyerOib, supplierOib, year, hasIndicator, severity, null, band, synthetic, sort, page, size);
    }
}
