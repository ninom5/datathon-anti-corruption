package com.datathon.api.source;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

public enum ExtractionMethod implements DatabaseCode {
    PARSER("parser"),
    LLM("llm"),
    MANUAL("manual"),
    API("api");

    private final String code;

    ExtractionMethod(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<ExtractionMethod> {
        public JpaConverter() {
            super(ExtractionMethod.class);
        }
    }
}
