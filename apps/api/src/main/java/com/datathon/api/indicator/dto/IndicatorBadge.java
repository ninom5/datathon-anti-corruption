package com.datathon.api.indicator.dto;

import com.datathon.api.indicator.Indicator;
import java.math.BigDecimal;

/** Short form of an indicator for table rows ("D +75 %"). */
public record IndicatorBadge(Long id, String ruleCode, BigDecimal magnitudeValue, String magnitudeLabel,
                             String magnitudeBand, String severityNote, boolean possibleEntryError) {

    public static IndicatorBadge from(Indicator indicator) {
        return new IndicatorBadge(indicator.getId(), indicator.getRuleCode(), indicator.getMagnitudeValue(),
                indicator.getMagnitudeLabel(),
                indicator.getMagnitudeBand() == null ? null : indicator.getMagnitudeBand().code(), indicator.getSeverityNote().code(),
                Indicator.isPossibleEntryError(indicator));
    }
}
