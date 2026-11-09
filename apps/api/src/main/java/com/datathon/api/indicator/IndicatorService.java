package com.datathon.api.indicator;

import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.indicator.dto.IndicatorContract;
import com.datathon.api.indicator.dto.IndicatorDetail;
import com.datathon.api.indicator.dto.IndicatorFilter;
import com.datathon.api.indicator.dto.IndicatorResponse;
import com.datathon.api.indicator.dto.IndicatorSubject;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.rules.EvidenceLink;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class IndicatorService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("ruleCode"),
            Sort.Order.desc("magnitudeValue").nullsLast(), Sort.Order.asc("id"));

    private final IndicatorRepository indicatorRepository;
    private final ContractRepository contractRepository;
    private final JsonMapper jsonMapper;

    @Transactional(readOnly = true)
    public PageResponse<IndicatorResponse> find(IndicatorFilter filter) {
        Page<Indicator> page = indicatorRepository.findAll(IndicatorSpecifications.matching(filter),
                PageRequests.of(filter.page(), filter.size(), DEFAULT_SORT));
        Map<Long, List<Long>> contractIds = contractIdsOf(page.getContent().stream().map(Indicator::getId).toList());
        return PageResponse.from(page, indicator -> toResponse(indicator, contractIds));
    }

    public List<IndicatorResponse> toResponses(List<Indicator> indicators) {
        Map<Long, List<Long>> contractIds = contractIdsOf(indicators.stream().map(Indicator::getId).toList());
        return indicators.stream().map(indicator -> toResponse(indicator, contractIds)).toList();
    }

    @Transactional(readOnly = true)
    public IndicatorDetail getById(Long id) {
        Indicator indicator = indicatorRepository.findDetailById(id)
                .filter(found -> found.getStatus() == IndicatorStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Indikator " + id + " ne postoji"));
        List<Long> contractIds = indicatorRepository.findContractIds(id);
        List<IndicatorContract> contracts = contractRepository.findByIdInOrderByDateSignedAscIdAsc(contractIds).stream()
                .filter(contract -> !isNaturalPerson(contract))
                .map(IndicatorContract::from)
                .toList();
        IndicatorResponse response = IndicatorResponse.from(indicator, evidenceOf(indicator), contractIds);
        return new IndicatorDetail(response, contracts, subjectOf(indicator));
    }

    @Transactional(readOnly = true)
    public boolean isActive(Long id) {
        return indicatorRepository.findById(id).filter(found -> found.getStatus() == IndicatorStatus.ACTIVE).isPresent();
    }

    private static boolean isNaturalPerson(Contract contract) {
        return contract.getSupplier() != null && contract.getSupplier().getOrgType() == OrganizationType.NATURAL_PERSON;
    }

    private IndicatorSubject subjectOf(Indicator indicator) {
        if (Indicator.SUBJECT_CONTRACT.equals(indicator.getSubjectType()) || indicator.getSubjectRef() == null) {
            return null;
        }
        return jsonMapper.readValue(indicator.getSubjectRef(), IndicatorSubject.class);
    }

    private IndicatorResponse toResponse(Indicator indicator, Map<Long, List<Long>> contractIds) {
        return IndicatorResponse.from(indicator, evidenceOf(indicator), contractIds.getOrDefault(indicator.getId(), List.of()));
    }

    private Map<Long, List<Long>> contractIdsOf(Collection<Long> indicatorIds) {
        Map<Long, List<Long>> byIndicator = new HashMap<>();
        if (indicatorIds.isEmpty()) {
            return byIndicator;
        }
        for (Object[] pair : indicatorRepository.findContractIdPairs(indicatorIds)) {
            byIndicator.computeIfAbsent((Long) pair[0], key -> new ArrayList<>()).add((Long) pair[1]);
        }
        return byIndicator;
    }

    private List<EvidenceLink> evidenceOf(Indicator indicator) {
        if (indicator.getEvidence() == null) {
            return List.of();
        }
        return List.of(jsonMapper.readValue(indicator.getEvidence(), EvidenceLink[].class));
    }
}
