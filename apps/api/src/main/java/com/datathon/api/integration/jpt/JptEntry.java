package com.datathon.api.integration.jpt;

import java.time.LocalDate;
import java.util.List;

public record JptEntry(
        String jptCode,
        String jptType,
        String oib,
        String name,
        LocalDate foundedOn,
        String county,
        String municipalityName,
        List<Founder> founders,
        String rawJson) {

    public record Founder(String oib, String name) {
    }
}
