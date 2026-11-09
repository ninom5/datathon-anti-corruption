package com.datathon.api.feedback;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

public enum FeedbackTargetType implements DatabaseCode {
    INDICATOR("indicator"),
    AI_ASSESSMENT("ai_assessment"),
    CONTRACT("contract"),
    ORGANIZATION("organization");

    private final String code;

    FeedbackTargetType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<FeedbackTargetType> {
        public JpaConverter() {
            super(FeedbackTargetType.class);
        }
    }
}
