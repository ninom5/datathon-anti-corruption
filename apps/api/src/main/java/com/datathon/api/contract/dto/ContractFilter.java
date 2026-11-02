package com.datathon.api.contract.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * Filters of the contract explorer. All are optional.
 *
 * @param severity  {@code vrijedi_provjeriti} or {@code informativno} (possible entry error in the source)
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
        Boolean synthetic,
        @Pattern(regexp = "dateSigned|value", message = "dopuštene vrijednosti su dateSigned i value") String sort,
        Integer page,
        Integer size) {

    public boolean includesSynthetic() {
        return Boolean.TRUE.equals(synthetic);
    }
}
