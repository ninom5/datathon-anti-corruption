package com.datathon.api.municipality.dto;

import java.math.BigDecimal;
import java.util.List;

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

    public record YearOption(int year, int withData, boolean incomplete) {
    }

    public record CategoryOption(String cpv2, String name, int municipalities, long contracts, BigDecimal totalValue) {
    }

    public record Member(String oib, String name, Integer population2021, long contractsInYear, long contracts,
                         BigDecimal totalValue, BigDecimal valuePerCapita, boolean selected) {
    }
}
