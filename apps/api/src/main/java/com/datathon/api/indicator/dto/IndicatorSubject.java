package com.datathon.api.indicator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * What a group indicator is about, parsed from {@code indicator.subject_ref} (docs/DATA_MODEL.md §2). Only the fields
 * of the indicator's own rule are present.
 *
 * @param cpv            F: the CPV group the contracts share
 * @param cpvLevel       F: 3 for a CPV group match, 2 when only the CPV division matches (weaker evidence)
 * @param from           F: first signing date in the window
 * @param to             F: last signing date in the window
 * @param threshold      F: simple procurement threshold (EUR, net of VAT) the sum is compared with, the lowest in the window
 * @param supplierOib    F: the shared supplier
 * @param cpv2           C: CPV division of the cell
 * @param years          C: the calendar years of the cell
 * @param topSupplierOib C: the supplier with the largest share
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record IndicatorSubject(
        String cpv,
        Integer cpvLevel,
        LocalDate from,
        LocalDate to,
        BigDecimal threshold,
        String supplierOib,
        String cpv2,
        List<Integer> years,
        String topSupplierOib) {
}
