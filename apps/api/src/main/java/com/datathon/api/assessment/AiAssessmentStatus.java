package com.datathon.api.assessment;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Optional;

/** ACCEPTED is the only status ever shown; REJECTED answers are kept for evaluation (§4.4); SUPERSEDED = replaced by a recheck. */
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
