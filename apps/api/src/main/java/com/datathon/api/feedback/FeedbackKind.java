package com.datathon.api.feedback;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

/** Ispravak, kontekst, pravo na odgovor ili zahtjev za uklanjanje; korisno/pogrešno are the AI assessment buttons (#44). */
public enum FeedbackKind implements DatabaseCode {
    DATA_ERROR("greska_u_podacima"),
    CONTEXT("kontekst"),
    RIGHT_OF_REPLY("pravo_na_odgovor"),
    USEFUL("korisno"),
    WRONG("pogresno"),
    PERSONAL_DATA_REMOVAL("uklanjanje_osobnih_podataka");

    private final String code;

    FeedbackKind(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<FeedbackKind> {
        public JpaConverter() {
            super(FeedbackKind.class);
        }
    }
}
