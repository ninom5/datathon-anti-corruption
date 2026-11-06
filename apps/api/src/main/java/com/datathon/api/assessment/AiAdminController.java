package com.datathon.api.assessment;

import com.datathon.api.assessment.eval.AiEvaluationService;
import com.datathon.api.common.AdminKeyGuard;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Runs the AI assessment (docs/ARCHITECTURE.md §4, step 9). Always do a dry run first: it shows the candidates, a
 * sample context and a cost estimate without calling the model.
 */
@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
@Validated
public class AiAdminController {

    private final AdminKeyGuard adminKeyGuard;
    private final AiRunService aiRunService;
    private final AiEvaluationService evaluationService;

    /**
     * @param synthetic true = only synthetic contracts (evaluation), false = only real ones
     * @param limit     at most this many candidates, never more than {@code max-contracts-per-run}
     */
    @PostMapping("/run")
    public AiRunResult run(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                           @RequestParam(required = false) @Min(0) Integer limit,
                           @RequestParam(defaultValue = "false") boolean synthetic,
                           @RequestParam(defaultValue = "true") boolean dryRun,
                           @RequestParam(required = false) List<Long> contractIds) {
        adminKeyGuard.verify(adminToken);
        return aiRunService.run(synthetic, limit, dryRun, contractIds == null ? List.of() : contractIds);
    }

    /** Runs the 40 synthetic evaluation cases (docs/ANOMALY_DETECTION.md §4.6); calls the model, costs ~0,07 USD. */
    @PostMapping("/eval/synthetic")
    public AiEvaluationService.SyntheticReport evaluateSynthetic(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        adminKeyGuard.verify(adminToken);
        return evaluationService.runSynthetic();
    }

    /** Reproducible random sample of real contracts as CSV for hand labelling (empty label columns). */
    @GetMapping(value = "/eval/real-sample", produces = "text/csv;charset=UTF-8")
    public String realSample(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                             @RequestParam(defaultValue = "44") String seed,
                             @RequestParam(defaultValue = "60") @Min(1) int size) {
        adminKeyGuard.verify(adminToken);
        // A byte order mark, so Excel opens the UTF-8 file with Croatian letters intact.
        return (char) 0xFEFF + evaluationService.realSampleCsv(seed, size);
    }

    /** AI run on exactly the contracts of the real sample (same seed and size as the CSV). */
    @PostMapping("/eval/real-sample/run")
    public AiRunResult runRealSample(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                                     @RequestParam(defaultValue = "44") String seed,
                                     @RequestParam(defaultValue = "60") @Min(1) int size,
                                     @RequestParam(defaultValue = "true") boolean dryRun) {
        adminKeyGuard.verify(adminToken);
        return aiRunService.run(false, null, dryRun, evaluationService.realSampleIds(seed, size));
    }

    /** Metrics on the labelled sample CSV (body); only labelled rows with an assessment of the current prompt count. */
    @PostMapping(value = "/eval/real", consumes = {"text/csv", MediaType.TEXT_PLAIN_VALUE})
    public AiEvaluationService.RealReport evaluateReal(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken, @RequestBody String csv) {
        adminKeyGuard.verify(adminToken);
        return evaluationService.realMetrics(csv);
    }
}
