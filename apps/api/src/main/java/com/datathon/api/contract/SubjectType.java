package com.datathon.api.contract;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

public enum SubjectType implements DatabaseCode {
    GOODS("robe"),
    SERVICES("usluge"),
    WORKS("radovi"),
    OTHER("ostalo");

    private final String code;

    SubjectType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<SubjectType> {
        public JpaConverter() {
            super(SubjectType.class);
        }
    }
}
