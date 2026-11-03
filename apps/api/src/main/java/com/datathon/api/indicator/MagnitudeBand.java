package com.datathon.api.indicator;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Optional;

/**
 * Band of the magnitude within one rule (bounds in thresholds.yaml, "bands"). A JavniRadar heuristic for filters and
 * reports; it is not a combined risk score and does not mix the three dimensions (work/decisions.md, 2026-11-03 #41).
 */
public enum MagnitudeBand implements DatabaseCode {
    LOW("nizak"),
    MEDIUM("srednji"),
    HIGH("visok");

    private final String code;

    MagnitudeBand(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    public static Optional<MagnitudeBand> fromCode(String code) {
        return Arrays.stream(values()).filter(band -> band.code.equals(code)).findFirst();
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<MagnitudeBand> {
        public JpaConverter() {
            super(MagnitudeBand.class);
        }
    }
}
