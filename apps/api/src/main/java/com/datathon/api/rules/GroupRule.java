package com.datathon.api.rules;

import java.util.List;

/**
 * A deterministic rule over all contracts of one buyer (F: splitting, C: concentration). Runs without any LLM key
 * (docs/ANOMALY_DETECTION.md §0.4). One finding becomes one indicator linked to every contract of its group.
 */
public interface GroupRule {

    String ruleCode();

    List<GroupFinding> evaluate(BuyerContracts buyerContracts);
}
