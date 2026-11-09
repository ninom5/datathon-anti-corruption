package com.datathon.api.organization;

import com.datathon.api.common.AdminKeyGuard;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sudreg")
@RequiredArgsConstructor
@Validated
public class CompanyRegisterAdminController {

    private final AdminKeyGuard adminKeyGuard;
    private final CompanyRegisterService companyRegisterService;

    @PostMapping("/run")
    public CompanyRegisterService.RunStatus run(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
                                                @RequestParam(required = false) @Min(1) Integer limit) {
        adminKeyGuard.verify(adminToken);
        return companyRegisterService.start(limit);
    }

    @GetMapping("/status")
    public CompanyRegisterService.RunStatus status(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        adminKeyGuard.verify(adminToken);
        return companyRegisterService.status();
    }
}
