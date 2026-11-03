package com.datathon.api.indicator.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

/**
 * The indicator detail page: every field of {@link IndicatorResponse} at the top level, plus the contracts of the group
 * and the parsed subject. Contracts with natural persons are never listed (GDPR).
 *
 * @param subject null for contract indicators (D1, D2), which are about a single contract
 */
public record IndicatorDetail(
        @JsonUnwrapped IndicatorResponse indicator,
        List<IndicatorContract> contracts,
        IndicatorSubject subject) {
}
