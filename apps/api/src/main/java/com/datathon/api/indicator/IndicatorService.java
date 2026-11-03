package com.datathon.api.indicator;

import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.indicator.dto.IndicatorFilter;
import com.datathon.api.indicator.dto.IndicatorResponse;
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

/** Read side of indicators: the indicator list and the shared mapping to the response with evidence and contracts. */
@Service
@RequiredArgsConstructor
public class IndicatorService {

    /** Rule first, then the largest deviation; units differ between rules, so values are only compared within one. */
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("ruleCode"),
            Sort.Order.desc("magnitudeValue").nullsLast(), Sort.Order.asc("id"));

    private final IndicatorRepository indicatorRepository;
    private final JsonMapper jsonMapper;

    @Transactional(readOnly = true)
    public PageResponse<IndicatorResponse> find(IndicatorFilter filter) {
        Page<Indicator> page = indicatorRepository.findAll(IndicatorSpecifications.matching(filter),
                PageRequests.of(filter.page(), filter.size(), DEFAULT_SORT));
        Map<Long, List<Long>> contractIds = contractIdsOf(page.getContent().stream().map(Indicator::getId).toList());
        return PageResponse.from(page, indicator -> toResponse(indicator, contractIds));
    }

    /** For callers inside a read transaction that already hold the indicators (contract detail, buyer profile). */
    public List<IndicatorResponse> toResponses(List<Indicator> indicators) {
        Map<Long, List<Long>> contractIds = contractIdsOf(indicators.stream().map(Indicator::getId).toList());
        return indicators.stream().map(indicator -> toResponse(indicator, contractIds)).toList();
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
