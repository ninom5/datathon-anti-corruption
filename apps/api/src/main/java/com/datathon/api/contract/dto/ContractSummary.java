package com.datathon.api.contract.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Totals for the explorer's top bar, over real data matching the filter. Synthetic contracts never enter these
 * numbers. Contracts with crafts are counted (a number does not reveal a person); they are left out only of lists
 * with names (decision 2026-11-03, #41).
 *
 * @param totalValue          sum of total values without VAT (initial value when the total is not published)
 * @param worthChecking       contracts with at least one "vrijedi provjeriti" indicator
 * @param possibleEntryErrors contracts with an "informativno" rule D indicator: probable entry errors in the source
 *                            ("informativno" F and C indicators are legitimate context, not errors)
 * @param craftContracts      how many of the counted contracts are with crafts (names hidden in lists)
 * @param contractsByRule     contracts with an active indicator of each rule (D_VALUE_INCREASE, D_OVERPAYMENT,
 *                            F_SPLITTING, C_CONCENTRATION), under the filter without its own rule choice
 * @param incompleteYears     years that were not over when the registers were retrieved
 * @param syntheticContracts  synthetic contracts matching the filter (only when they are shown, otherwise 0)
 */
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
