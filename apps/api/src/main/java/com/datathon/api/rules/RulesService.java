package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Recomputes all rules from scratch: contract rules (D1, D2) per contract, group rules (F, C) per buyer.
 * Deterministic: the same contracts always give the same indicators. Started after an ingest run, after loading the
 * synthetic set, or by hand from the admin endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {

    private final List<ContractRule> contractRules;
    private final List<GroupRule> groupRules;
    private final ContractRepository contractRepository;
    private final IndicatorRepository indicatorRepository;
    private final OrganizationRepository organizationRepository;
    private final MagnitudeBands magnitudeBands;
    private final JsonMapper jsonMapper;

    /** @return number of indicators per rule code, real and synthetic together */
    @Transactional
    public Map<String, Integer> runRules() {
        List<String> ruleCodes = Stream.concat(contractRules.stream().map(ContractRule::ruleCode),
                groupRules.stream().map(GroupRule::ruleCode)).toList();
        indicatorRepository.deleteByRules(ruleCodes, ContractRule.RULE_VERSION);
        List<Contract> contracts = contractRepository.findAllWithSource().stream()
                .filter(contract -> !isHiddenForPrivacy(contract))
                .toList();
        Map<String, Integer> counts = new TreeMap<>();
        runContractRules(contracts, counts);
        runGroupRules(contracts, counts);
        log.info("Rules {}: {}", ContractRule.RULE_VERSION, counts);
        return counts;
    }

    private void runContractRules(List<Contract> contracts, Map<String, Integer> counts) {
        for (Contract contract : contracts) {
            for (ContractRule rule : contractRules) {
                rule.evaluate(contract).ifPresent(draft -> {
                    indicatorRepository.save(toIndicator(contract, draft));
                    counts.merge(draft.ruleCode(), 1, Integer::sum);
                });
            }
        }
    }

    private void runGroupRules(List<Contract> contracts, Map<String, Integer> counts) {
        Set<String> founderPairs = new HashSet<>(organizationRepository.findSupplierFounderPairs());
        Map<Organization, List<Contract>> byBuyer = contracts.stream()
                .collect(Collectors.groupingBy(Contract::getBuyer, LinkedHashMap::new, Collectors.toList()));
        byBuyer.forEach((buyer, buyerContracts) -> {
            BuyerContracts input = new BuyerContracts(buyer, buyerContracts, founderPairs);
            for (GroupRule rule : groupRules) {
                for (GroupFinding finding : rule.evaluate(input)) {
                    indicatorRepository.save(toIndicator(buyer, finding));
                    counts.merge(finding.draft().ruleCode(), 1, Integer::sum);
                }
            }
        });
    }

    /** Contracts with natural persons are never shown (GDPR, AGENTS.md), so no indicator is computed for them. */
    private static boolean isHiddenForPrivacy(Contract contract) {
        return contract.getSupplier() != null && contract.getSupplier().getOrgType() == OrganizationType.NATURAL_PERSON;
    }

    private Indicator toIndicator(Contract contract, IndicatorDraft draft) {
        Indicator indicator = new Indicator(draft.ruleCode(), ContractRule.RULE_VERSION, contract);
        indicator.setSubjectRef(jsonMapper.writeValueAsString(Map.of("contractId", contract.getId())));
        fill(indicator, draft);
        indicator.setInputHash(sha256(contract.getNaturalKey() + "|" + draft.ruleCode() + "|" + draft.explanationHr()));
        return indicator;
    }

    private Indicator toIndicator(Organization buyer, GroupFinding finding) {
        List<Long> contractIds = finding.contracts().stream().map(Contract::getId).toList();
        Indicator indicator = new Indicator(finding.draft().ruleCode(), ContractRule.RULE_VERSION, finding.subjectType(),
                buyer, contractIds);
        indicator.setSubjectRef(jsonMapper.writeValueAsString(finding.subjectRef()));
        fill(indicator, finding.draft());
        String keys = finding.contracts().stream().map(Contract::getNaturalKey).sorted().collect(Collectors.joining(","));
        indicator.setInputHash(sha256(keys + "|" + finding.draft().ruleCode() + "|" + finding.draft().explanationHr()));
        return indicator;
    }

    private void fill(Indicator indicator, IndicatorDraft draft) {
        indicator.setMagnitudeValue(draft.magnitudeValue());
        indicator.setMagnitudeUnit(draft.magnitudeUnit());
        indicator.setMagnitudeLabel(draft.magnitudeLabel());
        indicator.setMagnitudeBand(magnitudeBands.of(draft.ruleCode(), draft.magnitudeValue()).orElse(null));
        indicator.setDataQuality(draft.dataQuality());
        indicator.setEvidenceStrength(draft.evidenceStrength());
        indicator.setSeverityNote(draft.severityNote());
        indicator.setExplanationHr(draft.explanationHr());
        indicator.setNotMeaningHr(draft.notMeaningHr());
        indicator.setMissingData(draft.missingData());
        indicator.setEvidence(jsonMapper.writeValueAsString(draft.evidence()));
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
