package com.datathon.api.assessment;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Optional;

public enum AiAssessmentStatus implements DatabaseCode {
    ACCEPTED("prihvaceno"),
    REJECTED("odbaceno_validacijom"),
    SUPERSEDED("zamijenjeno");

    private final String code;

    AiAssessmentStatus(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    public static Optional<AiAssessmentStatus> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst();
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<AiAssessmentStatus> {
        public JpaConverter() {
            super(AiAssessmentStatus.class);
        }
    }
}
