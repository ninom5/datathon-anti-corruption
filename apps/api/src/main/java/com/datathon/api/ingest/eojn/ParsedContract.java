package com.datathon.api.ingest.eojn;

import com.datathon.api.contract.SubjectType;
import com.datathon.api.ingest.normalize.Oibs;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ParsedContract(
        String contractUrl,
        String tenderUrl,
        String frameworkAgreementUrl,
        String buyerName,
        Oibs.Result buyerOib,
        String supplierName,
        Oibs.Result supplierOib,
        String evidNumber,
        String contractNumber,
        String subject,
        SubjectType subjectType,
        String cpv,
        String cpvLabel,
        String procedureType,
        Boolean euFunded,
        LocalDate dateSigned,
        String durationText,
        String currencyOriginal,
        BigDecimal initialValue,
        BigDecimal initialValueGross,
        BigDecimal amendmentsValue,
        BigDecimal totalValue,
        BigDecimal totalValueGross,
        BigDecimal paidAmount,
        LocalDate completedOn,
        String paymentNote,
        String note,
        String status,
        List<String> qualityFlags) {

    public boolean isFrameworkBased() {
        return frameworkAgreementUrl != null && !frameworkAgreementUrl.isBlank();
    }
}
