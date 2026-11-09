package com.datathon.api.contract.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ContractSummary(
        long contracts,
        BigDecimal totalValue,
        long buyers,
        long withIndicator,
        long worthChecking,
        long possibleEntryErrors,
        long craftContracts,
        Map<String, Long> contractsByRule,
        List<Integer> incompleteYears,
        long syntheticContracts) {
}
