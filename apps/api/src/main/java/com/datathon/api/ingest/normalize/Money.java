package com.datathon.api.ingest.normalize;

import com.datathon.api.ingest.extract.AmountParser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** Amount cells: Excel numbers ("198400.5") or Croatian text ("1.500,00 kn"); HRK is converted to EUR. */
public final class Money {

    /**
     * Fixed conversion rate, NN 85/2022 and Council Regulation (EU) 2022/1208 (docs/DATA_MODEL.md §5, VERIFICIRANO).
     * A legal constant, not a threshold, so it is not in thresholds.yaml.
     */
    public static final BigDecimal HRK_PER_EUR = new BigDecimal("7.53450");
    public static final LocalDate EURO_ADOPTION = LocalDate.of(2023, 1, 1);

    private static final Pattern EXCEL_NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
    private static final Pattern HRK_MARKER = Pattern.compile("\\bkn\\b|\\bhrk\\b|kuna", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERIODIC_MARKER = Pattern.compile("mjesečn|mjesecn|godišnj|godisnj|po satu", Pattern.CASE_INSENSITIVE);

    public enum Status { OK, MISSING, UNREADABLE, PERIODIC }

    /** {@code value} is already in EUR; {@code hrk} tells whether it was converted. */
    public record Result(BigDecimal value, Status status, boolean hrk) {
    }

    private Money() {
    }

    public static Result read(String raw, LocalDate dateSigned) {
        if (raw == null || raw.isBlank()) {
            return new Result(null, Status.MISSING, false);
        }
        String text = raw.strip();
        if (PERIODIC_MARKER.matcher(text).find()) {
            // A monthly amount must not be summed or multiplied (docs/DATA_MODEL.md §5).
            return new Result(null, Status.PERIODIC, false);
        }
        Optional<BigDecimal> parsed = parse(text);
        if (parsed.isEmpty()) {
            return new Result(null, Status.UNREADABLE, false);
        }
        boolean isHrk = HRK_MARKER.matcher(text).find() || (dateSigned != null && dateSigned.isBefore(EURO_ADOPTION));
        BigDecimal eur = isHrk ? parsed.get().divide(HRK_PER_EUR, 2, RoundingMode.HALF_UP) : parsed.get();
        return new Result(eur.setScale(2, RoundingMode.HALF_UP), Status.OK, isHrk);
    }

    private static Optional<BigDecimal> parse(String text) {
        String compact = text.replace(" ", "").toLowerCase(Locale.ROOT);
        if (EXCEL_NUMBER.matcher(compact).matches()) {
            return Optional.of(new BigDecimal(compact));
        }
        return AmountParser.parseStructured(text);
    }
}
