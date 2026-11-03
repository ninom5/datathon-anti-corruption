package com.datathon.api.rules;

import com.datathon.api.common.AdminKeyGuard;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Recomputes the contract rules without a new ingest, e.g. after changing thresholds.yaml. */
@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
public class RulesAdminController {

    private final AdminKeyGuard adminKeyGuard;
    private final RulesService rulesService;

    @PostMapping("/run")
    public Map<String, Integer> run(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        adminKeyGuard.verify(adminToken);
        return rulesService.runRules();
    }
}
