package com.datathon.api.assessment;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Optional;

public enum AiConfidence implements DatabaseCode {
    LOW("niska"),
    MEDIUM("srednja"),
    HIGH("visoka");

    private final String code;

    AiConfidence(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    public static Optional<AiConfidence> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst();
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<AiConfidence> {
        public JpaConverter() {
            super(AiConfidence.class);
        }
    }
}
