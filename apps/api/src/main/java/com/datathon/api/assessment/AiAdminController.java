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

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
@Validated
public class AiAdminController {

    private final AdminKeyGuard adminKeyGuard;
    private final AiRunService aiRunService;
    private final AiEvaluationService evaluationService;

    @PostMapping("/run")
    public AiRunResult run(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                           @RequestParam(required = false) @Min(0) Integer limit,
                           @RequestParam(defaultValue = "false") boolean synthetic,
                           @RequestParam(defaultValue = "true") boolean dryRun,
                           @RequestParam(required = false) List<Long> contractIds) {
        adminKeyGuard.verify(adminToken);
        return aiRunService.run(synthetic, limit, dryRun, contractIds == null ? List.of() : contractIds);
    }

    @PostMapping("/eval/synthetic")
    public AiEvaluationService.SyntheticReport evaluateSynthetic(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        adminKeyGuard.verify(adminToken);
        return evaluationService.runSynthetic();
    }

    @GetMapping(value = "/eval/real-sample", produces = "text/csv;charset=UTF-8")
    public String realSample(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                             @RequestParam(defaultValue = "44") String seed,
                             @RequestParam(defaultValue = "60") @Min(1) int size) {
        adminKeyGuard.verify(adminToken);
        return (char) 0xFEFF + evaluationService.realSampleCsv(seed, size);
    }

    @PostMapping("/eval/real-sample/run")
    public AiRunResult runRealSample(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                                     @RequestParam(defaultValue = "44") String seed,
                                     @RequestParam(defaultValue = "60") @Min(1) int size,
                                     @RequestParam(defaultValue = "true") boolean dryRun) {
        adminKeyGuard.verify(adminToken);
        return aiRunService.run(false, null, dryRun, evaluationService.realSampleIds(seed, size));
    }

    @PostMapping(value = "/eval/real", consumes = {"text/csv", MediaType.TEXT_PLAIN_VALUE})
    public AiEvaluationService.RealReport evaluateReal(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken, @RequestBody String csv) {
        adminKeyGuard.verify(adminToken);
        return evaluationService.realMetrics(csv);
    }
}
