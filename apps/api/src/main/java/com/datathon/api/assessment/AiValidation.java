package com.datathon.api.assessment;

import java.util.List;

/**
 * Result of {@link AiAssessmentValidator}: what is stored. A REJECTED answer is kept for evaluation, never shown.
 *
 * @param reasons         reasons that passed (all reasons of a wholly rejected answer, for evaluation)
 * @param rejectionReason why the answer or some of its reasons were rejected; null when everything passed
 */
public record AiValidation(AiAssessmentStatus status, AiLabel label, AiConfidence confidence,
                           List<AiAnswer.Reason> reasons, List<String> missingData, List<String> legitExplanations,
                           String rejectionReason) {

    public boolean isAccepted() {
        return status == AiAssessmentStatus.ACCEPTED;
    }
}
