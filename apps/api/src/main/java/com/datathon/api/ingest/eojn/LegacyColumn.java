package com.datathon.api.ingest.eojn;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum LegacyColumn {
    EVID_NUMBER("evidencijski broj nabave"),
    SUBJECT("predmet nabave"),
    CPV("cpv", "brojčana oznaka predmeta nabave"),
    NOTICE("broj objave iz eojn"),
    PROCEDURE_TYPE("vrsta postupka"),
    SUBCONTRACTORS("naziv i oib podugovaratelja"),
    SUPPLIER("naziv i oib ugovaratelja"),
    DATE_SIGNED("datum sklapanja"),
    CONTRACT_NUMBER("oznaka/broj ugovora"),
    DURATION("rok na koji je"),
    INITIAL_VALUE("iznos bez pdv-a"),
    VAT("iznos pdv-a"),
    INITIAL_VALUE_GROSS("ukupni iznos s pdv-om"),
    EU_FUNDED("ugovor se financira", "ugovor ili okvirni sporazum financira", "financira li se ugovor"),
    COMPLETED_ON("datum izvršenja"),
    PAID_AMOUNT("ukupni isplaćeni iznos"),
    PAYMENT_NOTE("obrazložen"),
    NOTE("napomena");

    private final List<String> prefixes;

    LegacyColumn(String... prefixes) {
        this.prefixes = List.of(prefixes);
    }

    public static Optional<LegacyColumn> fromHeader(String text) {
        String normalized = EojnColumn.normalizeHeader(text);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(column -> column.prefixes.stream().anyMatch(normalized::startsWith))
                .findFirst();
    }
}
