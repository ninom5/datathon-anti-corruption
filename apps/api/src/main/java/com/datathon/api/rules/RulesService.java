package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.organization.OrganizationType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Recomputes all contract rules from scratch. Deterministic: the same contracts always give the same indicators.
 * Started after an ingest run, after loading the synthetic set, or by hand from the admin endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {

    private final List<ContractRule> rules;
    private final ContractRepository contractRepository;
    private final IndicatorRepository indicatorRepository;
    private final JsonMapper jsonMapper;

    /** @return number of indicators per rule code, real and synthetic together */
    @Transactional
    public Map<String, Integer> runContractRules() {
        indicatorRepository.deleteByRules(rules.stream().map(ContractRule::ruleCode).toList(), ContractRule.RULE_VERSION);
        Map<String, Integer> counts = new TreeMap<>();
        for (Contract contract : contractRepository.findAllWithSource()) {
            if (isHiddenForPrivacy(contract)) {
                continue;
            }
            for (ContractRule rule : rules) {
                rule.evaluate(contract).ifPresent(draft -> {
                    indicatorRepository.save(toIndicator(contract, draft));
                    counts.merge(draft.ruleCode(), 1, Integer::sum);
                });
            }
        }
        log.info("Contract rules {}: {}", ContractRule.RULE_VERSION, counts);
        return counts;
    }

    /** Contracts with natural persons are never shown (GDPR, AGENTS.md), so no indicator is computed for them. */
    private static boolean isHiddenForPrivacy(Contract contract) {
        return contract.getSupplier() != null && contract.getSupplier().getOrgType() == OrganizationType.NATURAL_PERSON;
    }

    private Indicator toIndicator(Contract contract, IndicatorDraft draft) {
        Indicator indicator = new Indicator(draft.ruleCode(), ContractRule.RULE_VERSION, contract);
        indicator.setSubjectRef(jsonMapper.writeValueAsString(Map.of("contractId", contract.getId())));
        indicator.setMagnitudeValue(draft.magnitudeValue());
        indicator.setMagnitudeUnit(draft.magnitudeUnit());
        indicator.setMagnitudeLabel(draft.magnitudeLabel());
        indicator.setDataQuality(draft.dataQuality());
        indicator.setEvidenceStrength(draft.evidenceStrength());
        indicator.setSeverityNote(draft.severityNote());
        indicator.setExplanationHr(draft.explanationHr());
        indicator.setNotMeaningHr(draft.notMeaningHr());
        indicator.setMissingData(draft.missingData());
        indicator.setEvidence(jsonMapper.writeValueAsString(draft.evidence()));
        indicator.setInputHash(sha256(contract.getNaturalKey() + "|" + draft.ruleCode() + "|" + draft.explanationHr()));
        return indicator;
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
