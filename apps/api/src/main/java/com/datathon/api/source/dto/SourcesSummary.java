package com.datathon.api.source.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
