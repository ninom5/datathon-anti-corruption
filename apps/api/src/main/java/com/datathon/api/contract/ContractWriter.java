package com.datathon.api.contract;

import com.datathon.api.ingest.eojn.ParsedContract;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationResolver;
import com.datathon.api.source.Provenance;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Upserts a class A contract by its EOJN URL. The same contract appears in several yearly files (659 duplicates
 * measured on 2026-11-01), so a later file replaces the values and the provenance; the older raw row stays.
 */
@Service
@RequiredArgsConstructor
public class ContractWriter {

    public enum Outcome { CREATED, SUPERSEDED }

    /** {@code supersededProvenance} is the provenance the row had before, when it was replaced. */
    public record Written(Contract contract, Outcome outcome, Optional<Provenance> supersededProvenance) {
    }

    private final ContractRepository contractRepository;
    private final OrganizationResolver organizationResolver;

    public Written upsert(ParsedContract parsed, Provenance provenance, boolean isSynthetic) {
        Optional<Contract> existing = contractRepository.findByNaturalKey(parsed.contractUrl());
        Contract contract = existing.orElseGet(() -> new Contract(parsed.contractUrl()));
        Optional<Provenance> previous = existing.map(Contract::getProvenance);
        apply(contract, parsed, provenance, isSynthetic);
        contractRepository.save(contract);
        return new Written(contract, existing.isPresent() ? Outcome.SUPERSEDED : Outcome.CREATED, previous);
    }

    private void apply(Contract contract, ParsedContract parsed, Provenance provenance, boolean isSynthetic) {
        Organization buyer = organizationResolver.resolveBuyer(parsed.buyerName(), parsed.buyerOib(), isSynthetic, provenance);
        contract.setBuyer(buyer);
        contract.setSupplier(organizationResolver
                .resolveSupplier(parsed.supplierName(), parsed.supplierOib(), isSynthetic, provenance).orElse(null));
        contract.setEojnContractUrl(parsed.contractUrl());
        contract.setEojnTenderUrl(parsed.tenderUrl());
        contract.setFrameworkAgreementUrl(parsed.frameworkAgreementUrl());
        contract.setFrameworkBased(parsed.isFrameworkBased());
        contract.setEvidNumber(parsed.evidNumber());
        contract.setContractNumber(parsed.contractNumber());
        contract.setSubject(parsed.subject());
        contract.setSubjectType(parsed.subjectType());
        contract.setCpv(parsed.cpv());
        contract.setCpvLabel(parsed.cpvLabel());
        contract.setProcedureType(parsed.procedureType());
        contract.setEuFunded(parsed.euFunded());
        contract.setDateSigned(parsed.dateSigned());
        contract.setDurationText(parsed.durationText());
        contract.setCurrencyOriginal(parsed.currencyOriginal());
        applyAmounts(contract, parsed);
        contract.setCompletedOn(parsed.completedOn());
        contract.setPaymentNote(parsed.paymentNote());
        contract.setNote(parsed.note());
        contract.setStatus(parsed.status());
        contract.setQualityFlags(new ArrayList<>(parsed.qualityFlags()));
        contract.setSynthetic(isSynthetic);
        contract.setProvenance(provenance);
        contract.setValidFrom(Instant.now());
    }

    private static void applyAmounts(Contract contract, ParsedContract parsed) {
        contract.setInitialValue(parsed.initialValue());
        contract.setInitialValueGross(parsed.initialValueGross());
        contract.setAmendmentsValue(parsed.amendmentsValue());
        contract.setTotalValue(parsed.totalValue());
        contract.setTotalValueGross(parsed.totalValueGross());
        contract.setPaidAmount(parsed.paidAmount());
    }
}
