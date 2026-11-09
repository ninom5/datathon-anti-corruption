package com.datathon.api.indicator.dto;

import com.datathon.api.contract.Contract;
import com.datathon.api.organization.dto.OrganizationRef;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndicatorContract(
        Long id,
        LocalDate dateSigned,
        String subject,
        String cpv,
        OrganizationRef supplier,
        BigDecimal initialValue,
        BigDecimal totalValue,
        String procedureType) {

    public static IndicatorContract from(Contract contract) {
        return new IndicatorContract(contract.getId(), contract.getDateSigned(), contract.getSubject(), contract.getCpv(),
                OrganizationRef.from(contract.getSupplier()), contract.getInitialValue(), contract.getTotalValue(),
                contract.getProcedureType());
    }
}
