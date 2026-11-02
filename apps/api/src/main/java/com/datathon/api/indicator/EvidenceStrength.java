package com.datathon.api.indicator;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

/** Pouzdanost dokaza: how directly the data support the observation (docs/ANOMALY_DETECTION.md §2). */
public enum EvidenceStrength implements DatabaseCode {
    WEAK("slaba"),
    MEDIUM("srednja"),
    STRONG("jaka");

    private final String code;

    EvidenceStrength(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<EvidenceStrength> {
        public JpaConverter() {
            super(EvidenceStrength.class);
        }
    }
}
