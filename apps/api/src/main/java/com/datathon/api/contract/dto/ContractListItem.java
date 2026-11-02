package com.datathon.api.contract.dto;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.dto.IndicatorBadge;
import com.datathon.api.organization.dto.OrganizationRef;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One row of the contract explorer. Amounts are EUR without VAT; null means "not published".
 *
 * @param yearIncomplete the signing year was not over when the registers were retrieved, so its totals are partial
 */
public record ContractListItem(
        Long id,
        OrganizationRef buyer,
        OrganizationRef supplier,
        String subject,
        String subjectType,
        LocalDate dateSigned,
        Integer year,
        boolean yearIncomplete,
        BigDecimal initialValue,
        BigDecimal totalValue,
        BigDecimal paidAmount,
        List<IndicatorBadge> indicators,
        boolean synthetic) {

    public static ContractListItem from(Contract contract, List<IndicatorBadge> indicators, int incompleteFromYear) {
        Integer year = contract.getDateSigned() == null ? null : contract.getDateSigned().getYear();
        return new ContractListItem(contract.getId(), OrganizationRef.from(contract.getBuyer()),
                OrganizationRef.from(contract.getSupplier()), contract.getSubject(),
                contract.getSubjectType() == null ? null : contract.getSubjectType().code(), contract.getDateSigned(),
                year, year != null && year >= incompleteFromYear, contract.getInitialValue(), contract.getTotalValue(),
                contract.getPaidAmount(), indicators, contract.isSynthetic());
    }
}
