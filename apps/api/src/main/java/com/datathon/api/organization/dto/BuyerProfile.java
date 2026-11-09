package com.datathon.api.organization.dto;

import com.datathon.api.indicator.dto.IndicatorResponse;
import java.math.BigDecimal;
import java.util.List;

public record BuyerProfile(
        OrganizationRef buyer,
        Jpt jpt,
        MunicipalityInfo municipality,
        Coverage coverage,
        long contracts,
        BigDecimal totalValue,
        List<YearTotal> byYear,
        List<CpvTotal> byCpv,
        List<SupplierTotal> topSuppliers,
        long craftContracts,
        List<RuleCount> indicatorCounts,
        List<IndicatorResponse> indicators,
        List<SourceRef> sources,
        boolean synthetic) {

    public record Jpt(String code, String type, String county) {
    }

    public record MunicipalityInfo(String name, String kind, String county, Integer population2021, String peerGroup) {
    }

    public record Coverage(Integer fromYear, Integer toYear, List<Integer> years, List<Integer> incompleteYears, String label) {
    }

    public record YearTotal(int year, long contracts, BigDecimal totalValue, boolean incomplete) {
    }

    public record CpvTotal(String cpv2, long contracts, BigDecimal totalValue) {
    }

    public record SupplierTotal(OrganizationRef supplier, long contracts, BigDecimal totalValue) {
    }

    public record RuleCount(String ruleCode, String title, long total, long worthChecking, long informative) {
    }

    public record SourceRef(String datasetTitle, String landingUrl, String formatClass, long files, long contracts) {
    }
}
