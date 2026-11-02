package com.datathon.api.rules;

import com.datathon.api.indicator.DataQuality;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.SeverityNote;
import java.math.BigDecimal;
import java.util.List;

/** What a rule found for one contract, before it is stored. The three dimensions stay separate. */
public record IndicatorDraft(
        String ruleCode,
        BigDecimal magnitudeValue,
        String magnitudeUnit,
        String magnitudeLabel,
        DataQuality dataQuality,
        EvidenceStrength evidenceStrength,
        SeverityNote severityNote,
        String explanationHr,
        String notMeaningHr,
        List<String> missingData,
        List<EvidenceLink> evidence) {
}
