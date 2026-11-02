package com.datathon.api.contract;

import static com.datathon.api.contract.ContractSpecifications.realOnly;
import static com.datathon.api.contract.ContractSpecifications.syntheticOnly;
import static com.datathon.api.contract.ContractSpecifications.visible;
import static com.datathon.api.contract.ContractSpecifications.withCraft;
import static com.datathon.api.contract.ContractSpecifications.withIndicator;
import static com.datathon.api.contract.ContractSpecifications.withoutCrafts;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.contract.dto.ContractDetail;
import com.datathon.api.contract.dto.ContractFilter;
import com.datathon.api.contract.dto.ContractListItem;
import com.datathon.api.contract.dto.ContractSummary;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.indicator.IndicatorStatus;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.indicator.dto.IndicatorBadge;
import com.datathon.api.indicator.dto.IndicatorResponse;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.rules.EvidenceLink;
import com.datathon.api.source.DataCoverage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** Read side of contracts for the explorer and the detail page. */
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final IndicatorRepository indicatorRepository;
    private final ContractTotals contractTotals;
    private final DataCoverage dataCoverage;
    private final JsonMapper jsonMapper;

    @Transactional(readOnly = true)
    public PageResponse<ContractListItem> find(ContractFilter filter) {
        Page<Contract> page = contractRepository.findAll(visible(filter),
                PageRequests.of(filter.page(), filter.size(), sortOf(filter.sort())));
        Map<Long, List<IndicatorBadge>> badges = badgesByContract(page.getContent().stream().map(Contract::getId).toList());
        int incompleteFromYear = dataCoverage.incompleteFromYear();
        return PageResponse.from(page, contract ->
                ContractListItem.from(contract, badges.getOrDefault(contract.getId(), List.of()), incompleteFromYear));
    }

    @Transactional(readOnly = true)
    public ContractDetail getById(Long id) {
        Contract contract = contractRepository.findDetailById(id)
                .filter(ContractService::isShowable)
                .orElseThrow(() -> new NotFoundException("Ugovor " + id + " ne postoji."));
        List<IndicatorResponse> indicators = indicatorRepository
                .findByContractIdInAndStatusOrderByRuleCode(List.of(id), IndicatorStatus.ACTIVE).stream()
                .map(indicator -> IndicatorResponse.from(indicator, evidenceOf(indicator)))
                .toList();
        return ContractDetail.from(contract, indicators, dataCoverage.incompleteFromYear());
    }

    /** Real data only, without crafts (GDPR decision 2026-11-02); see {@link ContractSummary}. */
    @Transactional(readOnly = true)
    public ContractSummary summarize(ContractFilter filter) {
        Specification<Contract> real = visible(filter).and(realOnly());
        Specification<Contract> counted = real.and(withoutCrafts());
        ContractTotals.Totals totals = contractTotals.of(counted);
        return new ContractSummary(totals.contracts(), totals.totalValue(), totals.buyers(),
                contractRepository.count(counted.and(withIndicator(null))),
                contractRepository.count(counted.and(withIndicator(SeverityNote.WORTH_CHECKING))),
                contractRepository.count(counted.and(withIndicator(SeverityNote.INFORMATIVE))),
                contractRepository.count(real.and(withCraft())),
                List.of(dataCoverage.incompleteFromYear()),
                filter.includesSynthetic() ? contractRepository.count(visible(filter).and(syntheticOnly())) : 0);
    }

    /** Contracts with natural persons are not exposed (GDPR); a 404 does not reveal that they exist. */
    private static boolean isShowable(Contract contract) {
        return contract.getSupplier() == null || contract.getSupplier().getOrgType() != OrganizationType.NATURAL_PERSON;
    }

    private Map<Long, List<IndicatorBadge>> badgesByContract(List<Long> contractIds) {
        if (contractIds.isEmpty()) {
            return Map.of();
        }
        return indicatorRepository.findByContractIdInAndStatusOrderByRuleCode(contractIds, IndicatorStatus.ACTIVE).stream()
                .collect(Collectors.groupingBy(indicator -> indicator.getContract().getId(),
                        Collectors.mapping(IndicatorBadge::from, Collectors.toList())));
    }

    private List<EvidenceLink> evidenceOf(Indicator indicator) {
        if (indicator.getEvidence() == null) {
            return List.of();
        }
        return List.of(jsonMapper.readValue(indicator.getEvidence(), EvidenceLink[].class));
    }

    private static Sort sortOf(String sort) {
        if ("value".equals(sort)) {
            return Sort.by(Sort.Order.desc("totalValue").nullsLast(), Sort.Order.desc("id"));
        }
        return Sort.by(Sort.Order.desc("dateSigned").nullsLast(), Sort.Order.desc("id"));
    }
}
