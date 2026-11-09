package com.datathon.api.indicator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
