package com.datathon.api.municipality.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spending per inhabitant of one JLS next to its peer group (docs/ANOMALY_DETECTION.md §3A, level 2). Only JLS
 * with at least one contract signed in the year take part; a JLS without data is left out, never counted as 0.
 * Inside a JLS that has data for the year, a category without contracts is a real 0.
 *
 * @param cpv2                  CPV division the values are about, null for all categories
 * @param groupSize             all JLS of the peer group, with or without data
 * @param withData              JLS of the group with contracts signed in the year (n of the comparison)
 * @param sufficient            {@code withData >= minimumForComparison}; otherwise the UI shows
 *                              "Nedovoljno podataka za pouzdanu usporedbu"
 * @param medianPerCapita       median of {@code valuePerCapita} over members, null without members
 * @param selectedRank          1 = highest value per inhabitant; null when the JLS has no data for the year
 * @param members               members with data, highest value per inhabitant first
 */
public record PeerComparison(
        Municipality municipality,
        String peerGroup,
        String peerGroupLabel,
        int year,
        boolean yearIncomplete,
        List<YearOption> years,
        String cpv2,
        String cpvName,
        List<CategoryOption> categories,
        int groupSize,
        int withData,
        int minimumForComparison,
        boolean sufficient,
        BigDecimal medianPerCapita,
        Integer selectedRank,
        List<Member> members) {

    public record Municipality(String oib, String name, String kind, String county, Integer population2021) {
    }

    /** @param withData JLS of the group with contracts signed in that year */
    public record YearOption(int year, int withData, boolean incomplete) {
    }

    /** @param municipalities JLS of the group with contracts in this division in the year */
    public record CategoryOption(String cpv2, String name, int municipalities, long contracts, BigDecimal totalValue) {
    }

    /**
     * @param contractsInYear all contracts of the JLS signed in the year (shows that it has data)
     * @param contracts       contracts in the selected category
     * @param valuePerCapita  EUR per inhabitant, two decimals; null when the population is unknown
     */
    public record Member(String oib, String name, Integer population2021, long contractsInYear, long contracts,
                         BigDecimal totalValue, BigDecimal valuePerCapita, boolean selected) {
    }
}
