package com.datathon.api.contract.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Totals for the explorer's top bar, over real data matching the filter. Synthetic contracts never enter these
 * numbers, and neither do contracts with crafts (GDPR decision 2026-11-02): they are counted only in
 * {@code excludedCraftContracts} so the numbers can be reconciled with the table.
 *
 * @param totalValue              sum of total values without VAT (initial value when the total is not published)
 * @param worthChecking           contracts with at least one "vrijedi provjeriti" indicator
 * @param possibleEntryErrors     contracts with an "informativno" indicator: probable entry errors in the source
 * @param incompleteYears         years that were not over when the registers were retrieved
 * @param syntheticContracts      synthetic contracts matching the filter (only when they are shown, otherwise 0)
 */
public record ContractSummary(
        long contracts,
        BigDecimal totalValue,
        long buyers,
        long withIndicator,
        long worthChecking,
        long possibleEntryErrors,
        long excludedCraftContracts,
        List<Integer> incompleteYears,
        long syntheticContracts) {
}
