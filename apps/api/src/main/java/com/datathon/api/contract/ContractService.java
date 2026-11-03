package com.datathon.api.contract;

import static com.datathon.api.contract.ContractSpecifications.realOnly;
import static com.datathon.api.contract.ContractSpecifications.syntheticOnly;
import static com.datathon.api.contract.ContractSpecifications.visible;
import static com.datathon.api.contract.ContractSpecifications.withCraft;
import static com.datathon.api.contract.ContractSpecifications.withIndicator;
import static com.datathon.api.contract.ContractSpecifications.withIndicatorOfRule;
import static com.datathon.api.contract.ContractSpecifications.withPossibleEntryError;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.contract.dto.ContractDetail;
import com.datathon.api.contract.dto.ContractFilter;
import com.datathon.api.contract.dto.ContractListItem;
import com.datathon.api.contract.dto.ContractSummary;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.indicator.IndicatorService;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.indicator.dto.IndicatorBadge;
import com.datathon.api.indicator.dto.IndicatorResponse;
import com.datathon.api.indicator.dto.RuleCodes;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.source.DataCoverage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side of contracts for the explorer and the detail page. */
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorService indicatorService;
    private final ContractTotals contractTotals;
    private final DataCoverage dataCoverage;

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
        List<IndicatorResponse> indicators = indicatorService.toResponses(indicatorRepository.findActiveByContractId(id));
        return ContractDetail.from(contract, indicators, dataCoverage.incompleteFromYear());
    }

    /**
     * Real data only. Crafts are counted in the numbers, because a count does not reveal a person; they are left out
     * only of lists with names (decision 2026-11-03, #41). See {@link ContractSummary}.
     */
    @Transactional(readOnly = true)
    public ContractSummary summarize(ContractFilter filter) {
        Specification<Contract> counted = visible(filter).and(realOnly());
        ContractTotals.Totals totals = contractTotals.of(counted);
        return new ContractSummary(totals.contracts(), totals.totalValue(), totals.buyers(),
                contractRepository.count(counted.and(withIndicator(null))),
                contractRepository.count(counted.and(withIndicator(SeverityNote.WORTH_CHECKING))),
                contractRepository.count(counted.and(withPossibleEntryError())),
                contractRepository.count(counted.and(withCraft())),
                contractsByRule(filter),
                List.of(dataCoverage.incompleteFromYear()),
                filter.includesSynthetic() ? contractRepository.count(visible(filter).and(syntheticOnly())) : 0);
    }

    /** Counts next to each rule option: the filter without its own rule choice, so every option shows its number. */
    private Map<String, Long> contractsByRule(ContractFilter filter) {
        Specification<Contract> base = visible(filter.withoutRuleCodes()).and(realOnly());
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String ruleCode : RuleCodes.ALL) {
            counts.put(ruleCode, contractRepository.count(base.and(withIndicatorOfRule(ruleCode))));
        }
        return counts;
    }

    /** Whether the contract exists and may be shown, so other features (feedback) apply the same GDPR rule. */
    @Transactional(readOnly = true)
    public boolean isPublic(Long id) {
        return contractRepository.findById(id).filter(ContractService::isShowable).isPresent();
    }

    /** Contracts with natural persons are not exposed (GDPR); a 404 does not reveal that they exist. */
    private static boolean isShowable(Contract contract) {
        return contract.getSupplier() == null || contract.getSupplier().getOrgType() != OrganizationType.NATURAL_PERSON;
    }

    private Map<Long, List<IndicatorBadge>> badgesByContract(List<Long> contractIds) {
        if (contractIds.isEmpty()) {
            return Map.of();
        }
        return indicatorRepository.findActiveByContractIds(contractIds).stream()
                .collect(Collectors.groupingBy(IndicatorRepository.ContractIndicator::contractId,
                        Collectors.mapping(found -> IndicatorBadge.from(found.indicator()), Collectors.toList())));
    }

    private static Sort sortOf(String sort) {
        if ("value".equals(sort)) {
            return Sort.by(Sort.Order.desc("totalValue").nullsLast(), Sort.Order.desc("id"));
        }
        return Sort.by(Sort.Order.desc("dateSigned").nullsLast(), Sort.Order.desc("id"));
    }
}
