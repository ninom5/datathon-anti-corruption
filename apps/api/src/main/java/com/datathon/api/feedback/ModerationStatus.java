package com.datathon.api.feedback;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

public enum ModerationStatus implements DatabaseCode {
    NEW("novo"),
    PUBLISHED("objavljeno"),
    REJECTED("odbijeno");

    private final String code;

    ModerationStatus(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<ModerationStatus> {
        public JpaConverter() {
            super(ModerationStatus.class);
        }
    }
}
