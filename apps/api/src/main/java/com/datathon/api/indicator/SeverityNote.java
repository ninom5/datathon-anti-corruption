package com.datathon.api.indicator;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

/** Whether an indicator is worth checking or shown for information only (e.g. a likely data entry error). */
public enum SeverityNote implements DatabaseCode {
    INFORMATIVE("informativno"),
    WORTH_CHECKING("vrijedi_provjeriti");

    private final String code;

    SeverityNote(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    /** Unknown codes are rejected earlier by request validation. */
    public static SeverityNote fromCode(String code) {
        return INFORMATIVE.code.equals(code) ? INFORMATIVE : WORTH_CHECKING;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<SeverityNote> {
        public JpaConverter() {
            super(SeverityNote.class);
        }
    }
}
