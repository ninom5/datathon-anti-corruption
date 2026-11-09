package com.datathon.api.indicator.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

public record IndicatorDetail(
        @JsonUnwrapped IndicatorResponse indicator,
        List<IndicatorContract> contracts,
        IndicatorSubject subject) {
}
