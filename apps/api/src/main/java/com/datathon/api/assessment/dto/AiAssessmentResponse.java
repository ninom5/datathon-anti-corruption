package com.datathon.api.assessment.dto;

import java.time.Instant;
import java.util.List;

/**
 * The AI assessment of a contract as shown in the UI, always with the badge "AI procjena – zahtijeva ljudsku
 * provjeru" (AGENTS.md). Only an accepted assessment is ever returned.
 *
 * @param label      {@code nema_oznake}, {@code vrijedi_provjeriti} or {@code visoka_pozornost}
 * @param confidence {@code niska}, {@code srednja} or {@code visoka}
 */
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

    /** @param fields context paths ({@code ugovor.predmet}); @param sources URLs from the context */
    public record Reason(String claim, List<String> fields, List<String> sources) {
    }
}
