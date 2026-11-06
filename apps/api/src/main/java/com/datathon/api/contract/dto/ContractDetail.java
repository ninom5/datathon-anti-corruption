package com.datathon.api.contract.dto;

import com.datathon.api.assessment.dto.AiAssessmentResponse;
import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.dto.IndicatorResponse;
import com.datathon.api.organization.dto.OrganizationRef;
import com.datathon.api.source.dto.ProvenanceResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A contract with its values, rule indicators, the AI assessment (shown apart from the rules, never merged with them)
 * and the exact source row it was read from.
 *
 * @param aiAssessment                  null when there is no accepted assessment
 * @param aiAssessmentUnavailableReason why {@code aiAssessment} is null (e.g. crafts never get one); null otherwise
 */
public record ContractDetail(
        Long id,
        OrganizationRef buyer,
        OrganizationRef supplier,
        String subject,
        String subjectType,
        Cpv cpv,
        String procedureType,
        boolean frameworkBased,
        String contractNumber,
        LocalDate dateSigned,
        Integer year,
        boolean yearIncomplete,
        String durationText,
        LocalDate completedOn,
        Values values,
        String paymentNote,
        String note,
        Boolean euFunded,
        List<String> qualityFlags,
        String eojnContractUrl,
        String eojnTenderUrl,
        String frameworkAgreementUrl,
        List<IndicatorResponse> indicators,
        AiAssessmentResponse aiAssessment,
        String aiAssessmentUnavailableReason,
        ProvenanceResponse provenance,
        boolean synthetic) {

    public record Cpv(String code, String label) {
    }

    /**
     * EUR; "Gross" includes VAT. {@code convertedFromHrk} marks amounts the register gave in kuna (7,53450 HRK/EUR).
     */
    public record Values(BigDecimal initialValue, BigDecimal initialValueGross, BigDecimal amendmentsValue,
                         BigDecimal totalValue, BigDecimal totalValueGross, BigDecimal paidAmount, String currency,
                         boolean convertedFromHrk) {
    }

    public static ContractDetail from(Contract contract, List<IndicatorResponse> indicators,
                                      AiAssessmentResponse aiAssessment, String aiAssessmentUnavailableReason,
                                      int incompleteFromYear) {
        Integer year = contract.getDateSigned() == null ? null : contract.getDateSigned().getYear();
        return new ContractDetail(contract.getId(), OrganizationRef.from(contract.getBuyer()),
                OrganizationRef.from(contract.getSupplier()), contract.getSubject(),
                contract.getSubjectType() == null ? null : contract.getSubjectType().code(),
                contract.getCpv() == null ? null : new Cpv(contract.getCpv().strip(), contract.getCpvLabel()),
                contract.getProcedureType(), contract.isFrameworkBased(), contract.getContractNumber(),
                contract.getDateSigned(), year, year != null && year >= incompleteFromYear, contract.getDurationText(),
                contract.getCompletedOn(), values(contract), contract.getPaymentNote(), contract.getNote(),
                contract.getEuFunded(), List.copyOf(contract.getQualityFlags()), contract.getEojnContractUrl(),
                contract.getEojnTenderUrl(), contract.getFrameworkAgreementUrl(), indicators, aiAssessment,
                aiAssessmentUnavailableReason, ProvenanceResponse.from(contract.getProvenance()), contract.isSynthetic());
    }

    private static Values values(Contract contract) {
        return new Values(contract.getInitialValue(), contract.getInitialValueGross(), contract.getAmendmentsValue(),
                contract.getTotalValue(), contract.getTotalValueGross(), contract.getPaidAmount(), "EUR",
                "HRK".equals(contract.getCurrencyOriginal()));
    }
}
