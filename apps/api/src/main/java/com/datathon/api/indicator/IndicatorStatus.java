package com.datathon.api.indicator;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

public enum IndicatorStatus implements DatabaseCode {
    ACTIVE("aktivan"),
    DISPUTED("osporen"),
    CORRECTED("ispravljen"),
    WITHDRAWN("povucen");

    private final String code;

    IndicatorStatus(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<IndicatorStatus> {
        public JpaConverter() {
            super(IndicatorStatus.class);
        }
    }
}
