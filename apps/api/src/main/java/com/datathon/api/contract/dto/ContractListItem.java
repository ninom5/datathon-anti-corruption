package com.datathon.api.contract.dto;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.dto.IndicatorBadge;
import com.datathon.api.ingest.IssueCode;
import com.datathon.api.organization.dto.OrganizationRef;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * One row of the contract explorer. Amounts are EUR without VAT; null means "not published".
 *
 * @param yearIncomplete the signing year was not over when the registers were retrieved, so its totals are partial
 * @param amountDoubtful the register's own amounts contradict each other (VAT, shifted columns), so the shown amount may
 *                       be a typo in the source; the detail page lists the reasons
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
        boolean amountDoubtful,
        boolean synthetic) {

    private static final Set<String> DOUBTFUL_AMOUNT_FLAGS = Set.of(IssueCode.GROSS_BELOW_NET,
            IssueCode.VAT_RATIO_IMPLAUSIBLE, IssueCode.COLUMNS_SHIFTED);

    public static ContractListItem from(Contract contract, List<IndicatorBadge> indicators, int incompleteFromYear) {
        Integer year = contract.getDateSigned() == null ? null : contract.getDateSigned().getYear();
        return new ContractListItem(contract.getId(), OrganizationRef.from(contract.getBuyer()),
                OrganizationRef.from(contract.getSupplier()), contract.getSubject(),
                contract.getSubjectType() == null ? null : contract.getSubjectType().code(), contract.getDateSigned(),
                year, year != null && year >= incompleteFromYear, contract.getInitialValue(), contract.getTotalValue(),
                contract.getPaidAmount(), indicators,
                contract.getQualityFlags().stream().anyMatch(DOUBTFUL_AMOUNT_FLAGS::contains), contract.isSynthetic());
    }
}
