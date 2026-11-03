package com.datathon.api.organization.dto;

import com.datathon.api.indicator.dto.IndicatorResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * Profile of one buyer (docs/UI_UX.md, profil JLS). Numbers include contracts with crafts; lists with names
 * ({@code topSuppliers}) leave crafts out (decision 2026-11-03, #41). Contracts with natural persons are never counted.
 *
 * @param jpt          entry of the JPT register, null when the buyer is not in it
 * @param municipality DZS 2021 population and peer group, null when the buyer is not a JLS
 * @param byCpv        per CPV division (first two digits), largest value first
 * @param topSuppliers at most 10, without crafts; {@code craftContracts} says how many contracts are left out of it
 */
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

    /** @param kind {@code grad}, {@code opcina} or {@code grad_zagreb} */
    public record MunicipalityInfo(String name, String kind, String county, Integer population2021, String peerGroup) {
    }

    /** @param label e.g. "2018.–2026., 2026. nepotpuna" */
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

    /** @param formatClass A (EOJN export), A2 (old EOJN report) ... see docs/DATA_SOURCES.md §1 */
    public record SourceRef(String datasetTitle, String landingUrl, String formatClass, long files, long contracts) {
    }
}
