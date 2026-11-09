package com.datathon.api.rules;

import java.util.List;

public interface GroupRule {

    String ruleCode();

    List<GroupFinding> evaluate(BuyerContracts buyerContracts);
}
