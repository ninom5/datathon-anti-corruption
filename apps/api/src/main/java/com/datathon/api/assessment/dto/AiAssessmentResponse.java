package com.datathon.api.assessment.dto;

import java.time.Instant;
import java.util.List;

public record AiAssessmentResponse(
        Long id,
        String label,
        String confidence,
        List<Reason> reasons,
        List<String> missingData,
        List<String> legitExplanations,
        String model,
        String promptVersion,
        Instant createdAt,
        String badge) {

    public static final String BADGE = "AI procjena – zahtijeva ljudsku provjeru";

    public record Reason(String claim, List<String> fields, List<String> sources) {
    }
}
