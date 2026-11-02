package com.datathon.api.ingest.eojn;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Columns of the standard EOJN contract register export (format class A). Header texts are copied from the real
 * file "Grad Osijek / registar-ugovora.xlsx" downloaded 2026-11-02 (docs/DATA_SOURCES.md §1.2).
 */
public enum EojnColumn {
    CONTRACT_URL("Ugovor"),
    TENDER_URL("Postupak"),
    FRAMEWORK_AGREEMENT("Na osnovu okvirnog sporazuma"),
    BUYER_NAME("Naručitelj"),
    BUYER_OIB("OIB Naručitelja"),
    EVID_NUMBER("Evid. Broj"),
    SUBJECT("Predmet nabave"),
    SUBJECT_TYPE("Vrsta predmeta"),
    CPV("CPV"),
    PROCEDURE_TYPE("Vrsta postupka"),
    EU_FUNDED("EU Financ."),
    CONTRACT_NUMBER("Broj ugovora"),
    NOTICE("Objava"),
    LOT_NUMBER("Grupa br."),
    LOT_NAME("Naziv grupe"),
    SUPPLIER_NAME("Naziv ugovaratelja"),
    SUPPLIER_OIB("OIB ugovaratelja"),
    DATE_SIGNED("Datum sklapanja"),
    DURATION("Trajanje"),
    SUBCONTRACTORS("Podugovaratelj/i"),
    SUBCONTRACTOR_OIB("OIB podugovaratelja"),
    INITIAL_VALUE("Iznos bez PDV-a"),
    VAT("Iznos PDV-a"),
    INITIAL_VALUE_GROSS("Iznos s PDV-om"),
    AMENDMENT_VALUE("Iznos izmjene bez PDV-a"),
    AMENDMENT_VALUE_GROSS("Iznos izmjene s PDV-om"),
    TOTAL_VALUE("Ukupni iznos (sa izmjenama) bez PDV-a"),
    TOTAL_VALUE_GROSS("Ukupni iznos (sa izmjenama) s PDV-om"),
    STATUS("Status ugovora"),
    COMPLETED_ON("Datum izvršenja"),
    PAID_AMOUNT("Isplaćeni iznos"),
    PAYMENT_NOTE("Obrazloženje iznosa plaćanja"),
    NOTE("Napomena");

    private final String header;

    EojnColumn(String header) {
        this.header = header;
    }

    public String header() {
        return header;
    }

    public static Optional<EojnColumn> fromHeader(String text) {
        String normalized = normalizeHeader(text);
        return Arrays.stream(values()).filter(column -> normalizeHeader(column.header).equals(normalized)).findFirst();
    }

    /** The real file has e.g. "Iznos s PDV-om " with a trailing space, so whitespace and case are ignored. */
    static String normalizeHeader(String text) {
        return text == null ? "" : text.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
