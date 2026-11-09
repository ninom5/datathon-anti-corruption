package com.datathon.api.ingest.synthetic;

import com.datathon.api.common.AdminKeyGuard;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/synthetic")
@RequiredArgsConstructor
public class SyntheticAdminController {

    private final AdminKeyGuard adminKeyGuard;
    private final SyntheticRegisterLoader loader;

    @PostMapping("/load")
    public Map<String, Object> load(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        adminKeyGuard.verify(adminToken);
        return loader.load();
    }
}
