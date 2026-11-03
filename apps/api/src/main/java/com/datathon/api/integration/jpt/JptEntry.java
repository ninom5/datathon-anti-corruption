package com.datathon.api.integration.jpt;

import java.time.LocalDate;
import java.util.List;

/**
 * One public body from the JPT register, mapped from the API fields (Oznaka, VrstaJPT, OIB, Naziv, DatumOsnivanja,
 * Zupanija, GradOpcina, Osnivaci). {@code rawJson} is the original object, stored unchanged in raw_record.
 */
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

    /** An entry of "Osnivaci"; {@code oib} may be null (e.g. the Republic of Croatia is listed without one). */
    public record Founder(String oib, String name) {
    }
}
