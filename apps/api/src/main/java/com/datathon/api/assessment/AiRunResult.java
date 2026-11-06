package com.datathon.api.assessment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Answer of {@code POST /api/admin/ai/run}. In a dry run nothing is sent to the model: {@code toCall} and
 * {@code estimatedCostUsd} say what a real run would do.
 *
 * @param configured       false without ANTHROPIC_API_KEY; then nothing but a dry run is possible
 * @param eligible         contracts that meet the selection criteria, before the per-run cap
 * @param candidates       contracts chosen by {@link AiCandidateSelector} for this run
 * @param cacheHits        already assessed with the same model, prompt version and context: no call
 * @param toCall           dry run: contracts a real run would send to the model
 * @param called           model calls made
 * @param rejected         answers rejected by the validator (stored, never shown)
 * @param errors           calls without a usable answer (API error, cut-off answer); the contract stays without one
 * @param notProcessed     candidates left when the budget or the error limit stopped the run
 * @param stopReason       null when every candidate was processed
 * @param costUsd          cost of this run from the tokens and the configured prices
 * @param estimatedCostUsd dry run: rough estimate (characters / 3 per token, fixed output length), not a measurement
 */
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
