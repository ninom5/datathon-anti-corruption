package com.datathon.api.ingest.normalize;

import com.datathon.api.ingest.extract.CroatianDates;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

/** Date cells: ISO (date-formatted Excel cells), Excel serial numbers, or Croatian "dd.MM.yyyy.". */
public final class Dates {

    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 30);
    private static final Pattern ISO = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern SERIAL = Pattern.compile("\\d{5}(\\.\\d+)?");
    /** Serial numbers outside 1990-2100 are almost certainly not dates. */
    private static final int MIN_SERIAL = 32874;
    private static final int MAX_SERIAL = 73051;

    private Dates() {
    }

    public static Optional<LocalDate> read(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String text = raw.strip();
        if (ISO.matcher(text).matches()) {
            return parseIso(text);
        }
        if (SERIAL.matcher(text).matches()) {
            int serial = (int) Double.parseDouble(text);
            return serial >= MIN_SERIAL && serial <= MAX_SERIAL ? Optional.of(EXCEL_EPOCH.plusDays(serial)) : Optional.empty();
        }
        return CroatianDates.findFirst(text);
    }

    private static Optional<LocalDate> parseIso(String text) {
        try {
            return Optional.of(LocalDate.parse(text));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
