package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import java.util.Optional;

public interface ContractRule {

    String RULE_VERSION = "rules-v1";

    String ruleCode();

    Optional<IndicatorDraft> evaluate(Contract contract);
}
