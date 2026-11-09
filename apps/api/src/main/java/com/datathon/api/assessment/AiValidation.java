package com.datathon.api.assessment;

import java.util.List;

public record AiValidation(AiAssessmentStatus status, AiLabel label, AiConfidence confidence,
                           List<AiAnswer.Reason> reasons, List<String> missingData, List<String> legitExplanations,
                           String rejectionReason) {

    public boolean isAccepted() {
        return status == AiAssessmentStatus.ACCEPTED;
    }
}
