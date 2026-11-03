package com.datathon.api.feedback;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

/** Who is writing. Self-declared and unverified in the MVP (no accounts). */
public enum FeedbackRole implements DatabaseCode {
    CITIZEN("gradanin"),
    JOURNALIST("novinar"),
    BUYER("narucitelj"),
    SUPPLIER("dobavljac"),
    OTHER("ostalo");

    private final String code;

    FeedbackRole(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<FeedbackRole> {
        public JpaConverter() {
            super(FeedbackRole.class);
        }
    }
}
