package com.datathon.api.assessment;

import com.datathon.api.assessment.AiContextBuilder.CompanyFacts;
import com.datathon.api.assessment.AiContextBuilder.ContextFacts;
import com.datathon.api.organization.CompanySnapshotRepository;
import java.time.ZoneOffset;
import com.datathon.api.assessment.AiContextBuilder.MunicipalityFacts;
import com.datathon.api.assessment.AiContextBuilder.Threshold;
import com.datathon.api.contract.SubjectType;
import com.datathon.api.rules.RuleThresholds;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.municipality.MunicipalityService;
import com.datathon.api.municipality.dto.PeerComparison;
import com.datathon.api.organization.OrganizationRepository;
import com.datathon.api.reference.Municipality;
import com.datathon.api.reference.MunicipalityRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiContextLoader {

    private final ContractRepository contractRepository;
    private final IndicatorRepository indicatorRepository;
    private final OrganizationRepository organizationRepository;
    private final MunicipalityRepository municipalityRepository;
    private final MunicipalityService municipalityService;
    private final RuleThresholds thresholds;
    private final CompanySnapshotRepository snapshotRepository;

    public record Lookups(Set<String> supplierFounderPairs, Map<String, Optional<Map<String, Object>>> peerStatistics) {
    }

    @Transactional(readOnly = true)
    public Lookups lookups() {
        return new Lookups(new HashSet<>(organizationRepository.findSupplierFounderPairs()),
                new ConcurrentHashMap<>());
    }

    @Transactional(readOnly = true)
    public AiContext load(Long contractId, Lookups lookups) {
        Contract contract = contractRepository.findDetailById(contractId)
                .orElseThrow(() -> new NotFoundException("Ugovor " + contractId + " ne postoji."));
        Municipality municipality = contract.isSynthetic() ? null
                : municipalityRepository.findById(contract.getBuyer().getOib()).orElse(null);
        MunicipalityFacts municipalityFacts = municipality == null ? null
                : new MunicipalityFacts(municipality.getPopulation2021(), municipality.getPeerGroup());
        return AiContextBuilder.build(contract, indicatorRepository.findActiveByContractId(contractId),
                new ContextFacts(ownedByBuyer(contract, lookups), municipalityFacts, peerStatistics(municipality, contract, lookups),
                        simpleProcurementThreshold(contract), companyFacts(contract)));
    }

    private CompanyFacts companyFacts(Contract contract) {
        return snapshotRepository.findLatest(contract.getSupplier().getId())
                .map(snapshot -> new CompanyFacts(!snapshot.isNotFound(),
                        snapshot.getRetrievedAt().atZone(ZoneOffset.UTC).toLocalDate(), snapshot.getFoundedOn(),
                        snapshot.getMainNkd(), snapshot.getMainNkdName(), snapshot.getNkdVersion(),
                        snapshot.getInBankruptcy(), snapshot.getInLiquidation()))
                .orElse(null);
    }

    private Threshold simpleProcurementThreshold(Contract contract) {
        if (contract.getDateSigned() == null || contract.getSubjectType() == null
                || contract.getSubjectType() == SubjectType.OTHER) {
            return null;
        }
        return thresholds.simpleProcurementOn(contract.getDateSigned())
                .map(period -> new Threshold(period.limit(contract.getSubjectType()), period.source()))
                .orElse(null);
    }

    private static Boolean ownedByBuyer(Contract contract, Lookups lookups) {
        String pair = contract.getSupplier().getOib() + "|" + contract.getBuyer().getOib();
        return lookups.supplierFounderPairs().contains(pair) ? Boolean.TRUE : null;
    }

    private Map<String, Object> peerStatistics(Municipality municipality, Contract contract, Lookups lookups) {
        if (municipality == null || municipality.getPeerGroup() == null || contract.getDateSigned() == null
                || contract.getCpv() == null) {
            return null;
        }
        int year = contract.getDateSigned().getYear();
        String cpv2 = contract.getCpv().strip().substring(0, 2);
        String key = municipality.getOib() + "|" + year + "|" + cpv2;
        return lookups.peerStatistics()
                .computeIfAbsent(key, ignored -> comparePeers(municipality.getOib(), year, cpv2))
                .orElse(null);
    }

    private Optional<Map<String, Object>> comparePeers(String oib, int year, String cpv2) {
        PeerComparison comparison = municipalityService.comparePeers(oib, year, cpv2);
        if (!comparison.sufficient()) {
            return Optional.empty();
        }
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("godina", comparison.year());
        statistics.put("godina_nepotpuna", comparison.yearIncomplete());
        statistics.put("cpv2", cpv2);
        statistics.put("kategorija", comparison.cpvName());
        statistics.put("usporedna_skupina", comparison.peerGroup());
        statistics.put("n_opcina_s_podacima", comparison.withData());
        statistics.put("medijan_eur_po_stanovniku", comparison.medianPerCapita());
        statistics.put("ova_opcina_eur_po_stanovniku", comparison.members().stream()
                .filter(PeerComparison.Member::selected).findFirst()
                .map(PeerComparison.Member::valuePerCapita).orElse(null));
        statistics.put("rang", comparison.selectedRank());
        return Optional.of(statistics);
    }
}
