package com.datathon.api.assessment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AiRunResult(
        String model,
        String promptVersion,
        boolean dryRun,
        boolean synthetic,
        boolean configured,
        String message,
        int eligible,
        int candidates,
        int cacheHits,
        int toCall,
        int called,
        int accepted,
        int rejected,
        int errors,
        int notProcessed,
        String stopReason,
        BigDecimal costUsd,
        BigDecimal estimatedCostUsd,
        BigDecimal maxUsdPerRun,
        int maxCallsPerRun,
        List<AiCandidateSelector.Candidate> sampleCandidates,
        Map<String, Object> sampleContext) {
}
