package com.datathon.api.assessment;

import com.datathon.api.common.TooManyRequestsException;
import com.datathon.api.integration.anthropic.LlmClient;
import com.datathon.api.integration.anthropic.LlmUnavailableException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRunService {

    static final String NOT_CONFIGURED = "AI nije konfiguriran (ANTHROPIC_API_KEY nije postavljen).";
    static final int MAX_CONSECUTIVE_ERRORS = 5;
    private static final int CHARS_PER_TOKEN = 3;
    private static final int ESTIMATED_OUTPUT_TOKENS = 1500;
    private static final int SAMPLE_SIZE = 10;

    private final AiCandidateSelector candidateSelector;
    private final AiContextLoader contextLoader;
    private final AiAssessmentRepository assessmentRepository;
    private final AiAssessmentWriter writer;
    private final LlmClient llmClient;
    private final AiModelCall modelCall;
    private final AiProperties properties;

    private final AtomicBoolean running = new AtomicBoolean();

    public AiRunResult run(boolean synthetic, Integer limit, boolean dryRun) {
        return run(synthetic, limit, dryRun, List.of());
    }

    public AiRunResult run(boolean synthetic, Integer limit, boolean dryRun, List<Long> contractIds) {
        if (!running.compareAndSet(false, true)) {
            throw new TooManyRequestsException("AI run je već u tijeku.");
        }
        try {
            List<AiCandidateSelector.Candidate> candidates = contractIds.isEmpty()
                    ? candidateSelector.select(synthetic, limit) : candidateSelector.selectByIds(contractIds);
            return dryRun ? dryRun(synthetic, candidates) : realRun(synthetic, candidates);
        } finally {
            running.set(false);
        }
    }

    private AiRunResult dryRun(boolean synthetic, List<AiCandidateSelector.Candidate> candidates) {
        AiContextLoader.Lookups lookups = contextLoader.lookups();
        String model = properties.model();
        int cacheHits = 0;
        long inputChars = 0;
        AiContext sample = null;
        for (AiCandidateSelector.Candidate candidate : candidates) {
            AiContext context = contextLoader.load(candidate.contractId(), lookups);
            sample = sample == null ? context : sample;
            if (isCached(context, model)) {
                cacheHits++;
            } else {
                inputChars += AiPrompt.systemPrompt().length() + AiPrompt.userText(context.canonicalJson()).length();
            }
        }
        int toCall = Math.min(candidates.size() - cacheHits, properties.maxCallsPerRun());
        BigDecimal estimate = toCall == 0 ? BigDecimal.ZERO : properties.costUsd(model,
                inputChars / CHARS_PER_TOKEN * toCall / (candidates.size() - cacheHits),
                (long) ESTIMATED_OUTPUT_TOKENS * toCall);
        return result(model, synthetic, true, candidates, null, cacheHits, toCall, estimate, sample);
    }

    private AiRunResult realRun(boolean synthetic, List<AiCandidateSelector.Candidate> candidates) {
        String model = properties.model();
        if (!llmClient.isConfigured()) {
            return result(model, synthetic, false, candidates, null, 0, 0, null, null);
        }
        AiRunState state = new AiRunState(properties.maxUsdPerRun(), properties.maxCallsPerRun(), MAX_CONSECUTIVE_ERRORS);
        AiContextLoader.Lookups lookups = contextLoader.lookups();
        try (ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, properties.concurrency()))) {
            List<Future<?>> tasks = new ArrayList<>();
            for (AiCandidateSelector.Candidate candidate : candidates) {
                tasks.add(pool.submit(() -> process(candidate.contractId(), model, lookups, state)));
            }
            for (Future<?> task : tasks) {
                waitFor(task);
            }
        }
        log.info("AI run ({}, {}): {} candidates, {} from cache, {} calls, {} accepted, {} rejected, {} errors, {} USD{}",
                model, AiPrompt.VERSION, candidates.size(), state.cacheHits(), state.called(), state.accepted(),
                state.rejected(), state.errors(), state.costUsd(),
                state.stopReason() == null ? "" : ", stopped: " + state.stopReason());
        return result(model, synthetic, false, candidates, state, state.cacheHits(), 0, null, null);
    }

    private void process(Long contractId, String model, AiContextLoader.Lookups lookups, AiRunState state) {
        if (state.isStopped()) {
            state.skip();
            return;
        }
        try {
            AiContext context = contextLoader.load(contractId, lookups);
            if (isCached(context, model)) {
                writer.recordUsage(AiPrompt.PURPOSE, model, 0, 0, BigDecimal.ZERO, true);
                state.cacheHit();
                return;
            }
            if (!state.tryStartCall()) {
                state.skip();
                return;
            }
            assess(context, model, state);
        } catch (RuntimeException e) {
            log.warn("AI assessment of contract {} failed", contractId, e);
            state.error();
        }
    }

    private void assess(AiContext context, String model, AiRunState state) {
        String inputHash = ContextHash.of(AiPrompt.VERSION, model, context.canonicalJson());
        AiModelCall.Outcome outcome;
        try {
            outcome = modelCall.assess(context, model, AiPrompt.PURPOSE);
        } catch (AiModelCall.CallFailedException e) {
            state.addCost(e.costUsd());
            throw e;
        } catch (LlmUnavailableException e) {
            state.error();
            log.warn("Model unavailable for contract {}: {}", context.contractId(), e.getMessage());
            return;
        }
        state.addCost(outcome.costUsd());
        writer.save(context.contractId(), model, inputHash, outcome.validation());
        state.assessed(outcome.validation().isAccepted());
    }

    private boolean isCached(AiContext context, String model) {
        String inputHash = ContextHash.of(AiPrompt.VERSION, model, context.canonicalJson());
        return assessmentRepository.findCached(context.contractId(), model, AiPrompt.VERSION, inputHash).isPresent();
    }

    private static void waitFor(Future<?> task) {
        try {
            task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI run interrupted", e);
        } catch (java.util.concurrent.ExecutionException e) {
            log.warn("AI task failed: {}", e.getCause().getMessage());
        }
    }

    private AiRunResult result(String model, boolean synthetic, boolean dryRun,
                               List<AiCandidateSelector.Candidate> candidates, AiRunState state, int cacheHits,
                               int toCall, BigDecimal estimate, AiContext sample) {
        boolean isConfigured = llmClient.isConfigured();
        String message = !isConfigured ? NOT_CONFIGURED
                : dryRun ? "Probni run: model nije pozvan." : null;
        int eligible = candidateSelector.allEligible(synthetic).size();
        return new AiRunResult(model, AiPrompt.VERSION, dryRun, synthetic, isConfigured, message, eligible,
                candidates.size(),
                cacheHits, toCall, state == null ? 0 : state.called(), state == null ? 0 : state.accepted(),
                state == null ? 0 : state.rejected(), state == null ? 0 : state.errors(),
                state == null ? 0 : state.skipped(), state == null ? null : state.stopReason(),
                state == null ? BigDecimal.ZERO : state.costUsd(), estimate, properties.maxUsdPerRun(),
                properties.maxCallsPerRun(), candidates.stream().limit(SAMPLE_SIZE).toList(),
                sample == null ? null : sample.data());
    }
}
