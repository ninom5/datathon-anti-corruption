package com.datathon.api.indicator;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

/** Kvaliteta podataka: are the inputs complete and read by the parser (docs/ANOMALY_DETECTION.md §2). */
public enum DataQuality implements DatabaseCode {
    LOW("niska"),
    MEDIUM("srednja"),
    HIGH("visoka");

    private final String code;

    DataQuality(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<DataQuality> {
        public JpaConverter() {
            super(DataQuality.class);
        }
    }
}
