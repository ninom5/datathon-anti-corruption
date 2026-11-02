package com.datathon.api.ingest;

import com.datathon.api.common.AdminKeyGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Starts an ingest run by hand. Synchronous: a full run takes a few minutes (1 request/s per host). */
@RestController
@RequestMapping("/api/admin/ingest")
@RequiredArgsConstructor
public class IngestAdminController {

    private final AdminKeyGuard adminKeyGuard;
    private final IngestService ingestService;

    @PostMapping("/run")
    public IngestRunResponse run(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        adminKeyGuard.verify(adminToken);
        return IngestRunResponse.from(ingestService.run());
    }
}
