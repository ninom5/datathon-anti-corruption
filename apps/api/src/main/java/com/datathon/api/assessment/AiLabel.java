package com.datathon.api.assessment;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Optional;

public enum AiLabel implements DatabaseCode {
    NO_FLAG("nema_oznake"),
    WORTH_CHECKING("vrijedi_provjeriti"),
    HIGH_ATTENTION("visoka_pozornost");

    private final String code;

    AiLabel(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    public static Optional<AiLabel> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst();
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<AiLabel> {
        public JpaConverter() {
            super(AiLabel.class);
        }
    }
}
