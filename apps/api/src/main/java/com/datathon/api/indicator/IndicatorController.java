package com.datathon.api.indicator;

import com.datathon.api.common.PageResponse;
import com.datathon.api.indicator.dto.IndicatorFilter;
import com.datathon.api.indicator.dto.IndicatorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rule indicators across contracts and buyers (D1, D2, F, C), with their three separate dimensions. */
@RestController
@RequestMapping("/api/v1/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final IndicatorService indicatorService;

    @GetMapping
    public PageResponse<IndicatorResponse> list(@Valid @ModelAttribute IndicatorFilter filter) {
        return indicatorService.find(filter);
    }
}
