package com.datathon.api.source;

import com.datathon.api.common.PageResponse;
import com.datathon.api.source.dto.SourceDatasetResponse;
import com.datathon.api.source.dto.SourcesSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @GetMapping
    public PageResponse<SourceDatasetResponse> list(@RequestParam(defaultValue = "false") boolean synthetic,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer size) {
        return sourceService.list(synthetic, page, size);
    }

    @GetMapping("/summary")
    public SourcesSummary summary() {
        return sourceService.summarize();
    }
}
