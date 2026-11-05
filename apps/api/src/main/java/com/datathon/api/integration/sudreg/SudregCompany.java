package com.datathon.api.integration.sudreg;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * The fields of {@code /api/javni/detalji_subjekta} JavniRadar uses (checked 2026-11-04/05, docs/DATA_SOURCES.md §7).
 * The activity comes in two shapes: {@code pretezita_djelatnost} (NKD 2025 with name, e.g. "41.00.0") or, for older
 * entries, only the number {@code glavna_djelatnost} (6312 = "63.12") without name or version. The version is then
 * recorded as {@link #MAIN_ACTIVITY_FIELD}, never guessed.
 *
 * @param reducedPayload only these fields, without e-mail addresses or street, for provenance
 */
public record SudregCompany(String oib, String mbs, String name, LocalDate foundedOn, LocalDate dissolvedOn,
                            String legalForm, String nkdCode, String nkdName, String nkdVersion,
                            boolean inBankruptcy, boolean inLiquidation, String statusText,
                            Map<String, Object> reducedPayload) {

    public static final String MAIN_ACTIVITY_FIELD = "šifra iz polja glavna_djelatnost, bez naziva";
    private static final int MAIN_ACTIVITY_DIGITS = 4;

    static SudregCompany from(JsonNode answer) {
        JsonNode nkd = answer.path("pretezita_djelatnost").path("nacionalna_klasifikacija_djelatnosti");
        boolean hasPredominant = !nkd.path("sifra").asString("").isBlank();
        String mainActivity = mainActivityCode(answer.path("glavna_djelatnost"));
        String procedure = answer.path("postupak").path("postupak").path("znacenje").asString(null);
        String lowerProcedure = procedure == null ? "" : procedure.toLowerCase(Locale.ROOT);
        boolean isBankruptcy = answer.path("stecajna_masa").asInt(0) != 0 || lowerProcedure.contains("stečaj");
        boolean isLiquidation = answer.path("likvidacijska_masa").asInt(0) != 0 || lowerProcedure.contains("likvidacij");
        SudregCompany company = new SudregCompany(
                oib(text(answer, "oib")), text(answer, "potpuni_mbs"),
                answer.path("tvrtka").path("ime").asString(null),
                date(text(answer, "datum_osnivanja")).orElse(null),
                date(text(answer, "datum_brisanja")).orElse(null),
                answer.path("pravni_oblik").path("vrsta_pravnog_oblika").path("kratica").asString(null),
                hasPredominant ? nkd.path("sifra").asString(null) : mainActivity,
                hasPredominant ? nkd.path("puni_naziv").asString(null) : null,
                hasPredominant ? nkd.path("verzija").asString(null) : mainActivity == null ? null : MAIN_ACTIVITY_FIELD,
                isBankruptcy, isLiquidation, procedure, null);
        return company.withPayload();
    }

    private SudregCompany withPayload() {
        Map<String, Object> payload = new java.util.TreeMap<>();
        payload.put("oib", oib);
        payload.put("mbs", mbs);
        payload.put("tvrtka", name);
        payload.put("datum_osnivanja", foundedOn == null ? null : foundedOn.toString());
        payload.put("datum_brisanja", dissolvedOn == null ? null : dissolvedOn.toString());
        payload.put("pravni_oblik", legalForm);
        payload.put("nkd", nkdCode);
        payload.put("nkd_naziv", nkdName);
        payload.put("nkd_verzija", nkdVersion);
        payload.put("postupak", statusText);
        return new SudregCompany(oib, mbs, name, foundedOn, dissolvedOn, legalForm, nkdCode, nkdName, nkdVersion,
                inBankruptcy, inLiquidation, statusText, payload);
    }

    /** 6312 -> "63.12", 150 -> "01.50" (the number has no leading zero). */
    private static String mainActivityCode(JsonNode value) {
        if (!value.isNumber() || value.asInt() == 0) {
            return null;
        }
        String digits = String.format("%0" + MAIN_ACTIVITY_DIGITS + "d", value.asInt());
        return digits.substring(0, 2) + "." + digits.substring(2);
    }

    /** Sudreg sends the OIB as a JSON number, so a leading zero is lost and put back here. */
    private static String oib(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "0".repeat(Math.max(0, 11 - value.length())) + value;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNull() || value.isMissingNode() ? null : value.asString();
    }

    /** Sudreg dates look like "2020-12-09T00:00:00". */
    private static Optional<LocalDate> date(String value) {
        if (value == null || value.length() < 10) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(value.substring(0, 10)));
    }
}
