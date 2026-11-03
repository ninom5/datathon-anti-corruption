package com.datathon.api.indicator.dto;

import com.datathon.api.indicator.Indicator;
import com.datathon.api.organization.dto.OrganizationRef;
import com.datathon.api.rules.EvidenceLink;
import com.datathon.api.rules.OverpaymentRule;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * One rule indicator with its three separate dimensions. There is deliberately no combined score
 * (docs/ANOMALY_DETECTION.md §0.2).
 *
 * @param possibleEntryError {@code severityNote = informativno}: the numbers most likely contain an entry error in the
 *                           published register, so this is a data quality signal, not a signal about the contract
 * @param sourceExplanation  the buyer's own explanation from the register ("Obrazloženje iznosa plaćanja"), verbatim;
 *                           only for D2, null when the register gives none
 */
public record IndicatorResponse(
        Long id,
        String ruleCode,
        String ruleVersion,
        String title,
        String subjectType,
        OrganizationRef buyer,
        List<Long> contractIds,
        Magnitude magnitude,
        String dataQuality,
        String evidenceStrength,
        String severityNote,
        boolean possibleEntryError,
        String sourceExplanation,
        String explanation,
        String notMeaning,
        List<String> missingData,
        List<EvidenceLink> evidence,
        Instant computedAt,
        boolean synthetic) {

    /** @param band {@code nizak}, {@code srednji} or {@code visok} within the rule; a filter aid, not a score */
    public record Magnitude(BigDecimal value, String unit, String label, String band) {
    }

    public static IndicatorResponse from(Indicator indicator, List<EvidenceLink> evidence, List<Long> contractIds) {
        return new IndicatorResponse(indicator.getId(), indicator.getRuleCode(), indicator.getRuleVersion(),
                RuleTitles.of(indicator.getRuleCode()), indicator.getSubjectType(), OrganizationRef.from(indicator.getBuyer()),
                List.copyOf(contractIds),
                new Magnitude(indicator.getMagnitudeValue(), indicator.getMagnitudeUnit(), indicator.getMagnitudeLabel(),
                        indicator.getMagnitudeBand() == null ? null : indicator.getMagnitudeBand().code()),
                indicator.getDataQuality().code(), indicator.getEvidenceStrength().code(), indicator.getSeverityNote().code(),
                Indicator.isPossibleEntryError(indicator), sourceExplanation(indicator),
                indicator.getExplanationHr(), indicator.getNotMeaningHr(), List.copyOf(indicator.getMissingData()), evidence,
                indicator.getComputedAt(), indicator.getBuyer() != null && indicator.getBuyer().isSynthetic());
    }

    private static String sourceExplanation(Indicator indicator) {
        if (!OverpaymentRule.RULE_CODE.equals(indicator.getRuleCode())) {
            return null;
        }
        String note = indicator.getContract().getPaymentNote();
        return note == null || note.isBlank() ? null : note.strip();
    }
}
