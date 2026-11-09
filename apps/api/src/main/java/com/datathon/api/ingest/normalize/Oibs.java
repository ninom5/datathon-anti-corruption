package com.datathon.api.ingest.normalize;

import com.datathon.api.common.OibValidator;

public final class Oibs {

    private static final String SYNTHETIC_PREFIX = "000000000";

    public enum Status { VALID, SYNTHETIC_RESERVED, INVALID_CHECKSUM, MALFORMED, MISSING }

    public record Result(String oib, Status status) {

        public boolean isUsable() {
            return oib != null;
        }
    }

    private Oibs() {
    }

    public static Result read(String raw, boolean isSyntheticSource) {
        if (raw == null || raw.isBlank()) {
            return new Result(null, Status.MISSING);
        }
        String digits = raw.replaceAll("\\s", "");
        if (digits.matches("\\d{10}") && OibValidator.isValidOib("0" + digits)) {
            digits = "0" + digits;
        }
        if (!digits.matches("\\d{11}")) {
            return new Result(null, Status.MALFORMED);
        }
        if (isSyntheticSource && digits.startsWith(SYNTHETIC_PREFIX)) {
            return new Result(digits, Status.SYNTHETIC_RESERVED);
        }
        return new Result(digits, OibValidator.isValidOib(digits) ? Status.VALID : Status.INVALID_CHECKSUM);
    }
}
