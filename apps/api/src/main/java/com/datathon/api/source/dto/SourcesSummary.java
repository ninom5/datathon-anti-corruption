package com.datathon.api.source.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Coverage of real data (synthetic data excluded), for the sources page.
 *
 * @param filesByStatus      file count per {@link SourceFileResponse#status()}
 * @param buyersByType       buyers with contracts per type ({@code jls}, {@code zupanija}, {@code javno_tijelo})
 * @param contractsByYear    contracts per signing year; years from {@code incompleteFromYear} on are partial
 * @param hiddenContracts    contracts with natural persons, kept out of the API (GDPR)
 * @param issuesByCode       ingest issues per code (row rejections and warnings, docs/ARCHITECTURE.md §6)
 */
public record SourcesSummary(
        long datasets,
        long files,
        Map<String, Long> filesByStatus,
        long rows,
        long contracts,
        Map<String, Long> buyersByType,
        List<YearCount> contractsByYear,
        int incompleteFromYear,
        long hiddenContracts,
        Map<String, Long> issuesByCode,
        Instant latestRetrievedAt) {

    public record YearCount(int year, long contracts, boolean incomplete) {
    }
}
