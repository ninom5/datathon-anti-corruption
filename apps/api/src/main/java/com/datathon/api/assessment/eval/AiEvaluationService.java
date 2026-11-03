package com.datathon.api.assessment.eval;

import com.datathon.api.assessment.AiAssessment;
import com.datathon.api.assessment.AiAssessmentRepository;
import com.datathon.api.assessment.AiAssessmentStatus;
import com.datathon.api.assessment.AiContext;
import com.datathon.api.assessment.AiLabel;
import com.datathon.api.assessment.AiModelCall;
import com.datathon.api.assessment.AiPrompt;
import com.datathon.api.assessment.AiProperties;
import com.datathon.api.assessment.AiValidation;
import com.datathon.api.assessment.eval.EvalMetrics.Pair;
import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.integration.anthropic.LlmClient;
import com.datathon.api.organization.OrganizationType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Evaluation of the AI assessment (docs/ANOMALY_DETECTION.md §4.6). Synthetic: 40 fixed contexts with labels written
 * before any run. Real: a reproducible random sample that people label by hand; metrics are computed only on what was
 * actually labelled. Results go to docs/EVALUATION.md by hand, with date, model, prompt version, n and cost.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiEvaluationService {

    public static final String SYNTHETIC_CASES = "ai/eval/synthetic-cases-v4.json";
    static final String PURPOSE = "ai-eval";
    static final String LABEL_FLAG = "vrijedi_provjeriti";
    static final String LABEL_NO_FLAG = "ne";

    private final AiModelCall modelCall;
    private final AiProperties properties;
    private final LlmClient llmClient;
    private final AiAssessmentRepository assessmentRepository;
    private final IndicatorRepository indicatorRepository;
    private final JsonMapper jsonMapper;

    public record CaseResult(String id, String scenario, String expected, String actual, String status,
                             String rejectionReason, List<String> claims, String error) {
    }

    public record SyntheticReport(String casesVersion, String model, String promptVersion, Instant runAt, int cases,
                                  EvalMetrics flagMetrics, BigDecimal exactLabelAgreement, int rejectedByValidator,
                                  int partlyRejected, int errors, BigDecimal costUsd, List<CaseResult> results) {
    }

    public SyntheticReport runSynthetic() {
        if (!llmClient.isConfigured()) {
            throw new InvalidRequestException("AI nije konfiguriran (ANTHROPIC_API_KEY nije postavljen).");
        }
        JsonNode file = readCases();
        String model = properties.model();
        List<CaseResult> results = new ArrayList<>();
        BigDecimal[] cost = {BigDecimal.ZERO};
        try (ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, properties.concurrency()))) {
            List<Future<CaseResult>> tasks = new ArrayList<>();
            int index = 0;
            for (JsonNode testCase : file.path("cases")) {
                long syntheticId = -(++index);
                tasks.add(pool.submit(() -> runCase(testCase, syntheticId, model, cost)));
            }
            for (Future<CaseResult> task : tasks) {
                results.add(task.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Evaluation interrupted", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IllegalStateException("Evaluation failed", e.getCause());
        }
        results.sort(Comparator.comparing(CaseResult::id));
        return report(file.path("version").asString(), model, results, cost[0]);
    }

    private CaseResult runCase(JsonNode testCase, long syntheticId, String model, BigDecimal[] cost) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = jsonMapper.treeToValue(testCase.path("context"), Map.class);
        JsonNode supplier = testCase.path("context").path("dobavljac");
        AiContext context = AiContext.of(syntheticId, data, organizationType(supplier.path("pravni_oblik").asString()),
                supplier.path("naziv").asString());
        String id = testCase.path("id").asString();
        String scenario = testCase.path("scenario").asString();
        String expected = testCase.path("expected").asString();
        try {
            AiModelCall.Outcome outcome = modelCall.assess(context, model, PURPOSE);
            addCost(cost, outcome.costUsd());
            AiValidation validation = outcome.validation();
            return new CaseResult(id, scenario, expected, validation.label().code(), validation.status().code(),
                    validation.rejectionReason(), validation.reasons().stream().map(reason -> reason.claim()).toList(), null);
        } catch (AiModelCall.CallFailedException e) {
            addCost(cost, e.costUsd());
            return new CaseResult(id, scenario, expected, null, null, null, List.of(), e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Evaluation case {} failed", id, e);
            return new CaseResult(id, scenario, expected, null, null, null, List.of(), e.getClass().getSimpleName());
        }
    }

    private static synchronized void addCost(BigDecimal[] cost, BigDecimal amount) {
        cost[0] = cost[0].add(amount);
    }

    /** A rejected answer counts as "no flag", because that is what the user would see (nothing). */
    private static SyntheticReport report(String version, String model, List<CaseResult> results, BigDecimal cost) {
        List<CaseResult> answered = results.stream().filter(result -> result.actual() != null).toList();
        List<Pair> pairs = answered.stream()
                .map(result -> new Pair(isFlag(result.expected()), isShownFlag(result))).toList();
        long exact = answered.stream()
                .filter(result -> result.expected().equals(isShown(result) ? result.actual() : AiLabel.NO_FLAG.code()))
                .count();
        int rejected = (int) answered.stream().filter(result -> !isShown(result)).count();
        int partly = (int) answered.stream().filter(result -> isShown(result) && result.rejectionReason() != null).count();
        return new SyntheticReport(version, model, AiPrompt.VERSION, Instant.now(), results.size(), EvalMetrics.of(pairs),
                EvalMetrics.ratio((int) exact, answered.size()), rejected, partly, results.size() - answered.size(), cost,
                results);
    }

    private static boolean isShown(CaseResult result) {
        return AiAssessmentStatus.ACCEPTED.code().equals(result.status());
    }

    private static boolean isShownFlag(CaseResult result) {
        return isShown(result) && isFlag(result.actual());
    }

    private static boolean isFlag(String label) {
        return label != null && !AiLabel.NO_FLAG.code().equals(label);
    }

    private static OrganizationType organizationType(String code) {
        return Arrays.stream(OrganizationType.values()).filter(type -> type.code().equals(code)).findFirst()
                .orElse(OrganizationType.UNKNOWN);
    }

    private JsonNode readCases() {
        try (InputStream in = new ClassPathResource(SYNTHETIC_CASES).getInputStream()) {
            return jsonMapper.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ----- real sample -----

    /** CSV (semicolon, UTF-8) for hand labelling; the label columns are empty on purpose. */
    @Transactional(readOnly = true)
    public String realSampleCsv(String seed, int size) {
        StringBuilder csv = new StringBuilder(
                "id;narucitelj;predmet;iznos_eur;datum;vrsta_postupka;izvor;javniradar;oznaka_osoba1;oznaka_osoba2\n");
        for (Object[] row : assessmentRepository.findRealSample(seed, size)) {
            String source = row[7] == null ? (String) row[6] : row[6] + "#" + row[7];
            csv.append(String.join(";", cell(row[0]), cell(row[1]), cell(row[2]), cell(row[3]), cell(row[4]),
                    cell(row[5]), cell(source), cell("/ugovor/" + row[0]), "", "")).append('\n');
        }
        return csv.toString();
    }

    public List<Long> realSampleIds(String seed, int size) {
        return assessmentRepository.findRealSample(seed, size).stream().map(row -> ((Number) row[0]).longValue()).toList();
    }

    private static String cell(Object value) {
        String text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"").replace("\n", " ") + "\"";
    }

    public record RealReport(String model, String promptVersion, int rows, int withAssessment, int labelledByBoth,
                             EvalMetrics person1, EvalMetrics person2, EvalMetrics consensus,
                             BigDecimal interAnnotatorKappa, BigDecimal aiVersusRulesKappa, int aiVersusRulesN,
                             BigDecimal rejectionRate) {
    }

    /**
     * Metrics on a labelled sample CSV (columns id, oznaka_osoba1, oznaka_osoba2; labels "vrijedi_provjeriti" or "ne").
     * Unlabelled rows and contracts without an assessment are left out, never counted as 0.
     */
    @Transactional(readOnly = true)
    public RealReport realMetrics(String csv) {
        List<Map<String, String>> rows = SemicolonCsv.parse(csv);
        List<Pair> person1 = new ArrayList<>();
        List<Pair> person2 = new ArrayList<>();
        List<Pair> consensus = new ArrayList<>();
        List<Pair> annotators = new ArrayList<>();
        List<Pair> rules = new ArrayList<>();
        int withAssessment = 0;
        int rejected = 0;
        for (Map<String, String> row : rows) {
            Optional<AiAssessment> latest = assessmentRepository.findLatest(Long.parseLong(row.get("id").strip()));
            if (latest.isEmpty() || !AiPrompt.VERSION.equals(latest.get().getPromptVersion())) {
                continue;
            }
            withAssessment++;
            boolean isShown = latest.get().getStatus() == AiAssessmentStatus.ACCEPTED;
            rejected += isShown ? 0 : 1;
            boolean aiFlag = isShown && latest.get().getLabel() != AiLabel.NO_FLAG;
            Optional<Boolean> first = humanLabel(row.get("oznaka_osoba1"));
            Optional<Boolean> second = humanLabel(row.get("oznaka_osoba2"));
            first.ifPresent(label -> person1.add(new Pair(label, aiFlag)));
            second.ifPresent(label -> person2.add(new Pair(label, aiFlag)));
            if (first.isPresent() && second.isPresent()) {
                annotators.add(new Pair(first.get(), second.get()));
                if (first.get().equals(second.get())) {
                    consensus.add(new Pair(first.get(), aiFlag));
                }
            }
            rules.add(new Pair(hasWorthCheckingIndicator(latest.get().getContract().getId()), aiFlag));
        }
        return new RealReport(properties.model(), AiPrompt.VERSION, rows.size(), withAssessment, annotators.size(),
                EvalMetrics.of(person1), EvalMetrics.of(person2), EvalMetrics.of(consensus),
                EvalMetrics.cohensKappa(annotators), EvalMetrics.cohensKappa(rules), rules.size(),
                EvalMetrics.ratio(rejected, withAssessment));
    }

    private boolean hasWorthCheckingIndicator(Long contractId) {
        return indicatorRepository.findActiveByContractId(contractId).stream()
                .anyMatch(indicator -> indicator.getSeverityNote() == SeverityNote.WORTH_CHECKING);
    }

    private static Optional<Boolean> humanLabel(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String label = value.strip().toLowerCase(java.util.Locale.ROOT);
        if (label.equals(LABEL_FLAG)) {
            return Optional.of(true);
        }
        if (label.equals(LABEL_NO_FLAG)) {
            return Optional.of(false);
        }
        throw new InvalidRequestException("Nepoznata oznaka \"" + value + "\": dopušteno je \"" + LABEL_FLAG
                + "\" ili \"" + LABEL_NO_FLAG + "\".");
    }
}
