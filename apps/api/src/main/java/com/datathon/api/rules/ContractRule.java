package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import java.util.Optional;

/** A deterministic rule on one contract. Runs without any LLM key (docs/ANOMALY_DETECTION.md §0.4). */
public interface ContractRule {

    String RULE_VERSION = "rules-v1";

    String ruleCode();

    Optional<IndicatorDraft> evaluate(Contract contract);
}
