package com.datathon.api.ingest.normalize;

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
    /** An explicit euro marker wins over the signing date: later reports restate old contracts in EUR. */
    private static final Pattern EUR_MARKER = Pattern.compile("\\beur\\b|€",Pattern.CASE_INSENSITIVE);
    private static final Pattern PERIODIC_MARKER = Pattern.compile("mjesečn|mjesecn|godišnj|godisnj|po satu", Pattern.CASE_INSENSITIVE);

    public enum Status { OK, MISSING, UNREADABLE, PERIODIC }

    /** {@code value} is already in EUR; {@code hrk} tells whether it was converted. */
    public record Result(BigDecimal value, Status status, boolean hrk) {
    }

    private Money() {
    }

    /** Currency named by the column header ("Iznos bez PDV-a (kn)") when the cell itself has no marker. */
    public enum HeaderCurrency { NONE, HRK, EUR }

    public static Result read(String raw, LocalDate dateSigned) {
        return read(raw, dateSigned, HeaderCurrency.NONE);
    }

    public static Result read(String raw, LocalDate dateSigned, HeaderCurrency headerCurrency) {
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
        boolean isHrk = isHrk(text, dateSigned, headerCurrency);
        BigDecimal eur = isHrk ? parsed.get().divide(HRK_PER_EUR, 2, RoundingMode.HALF_UP) : parsed.get();
        return new Result(eur.setScale(2, RoundingMode.HALF_UP), Status.OK, isHrk);
    }

    private static boolean isHrk(String text, LocalDate dateSigned, HeaderCurrency headerCurrency) {
        if (HRK_MARKER.matcher(text).find()) {
            return true;
        }
        if (EUR_MARKER.matcher(text).find()) {
            return false;
        }
        return switch (headerCurrency) {
            case HRK -> true;
            case EUR -> false;
            case NONE -> dateSigned != null && dateSigned.isBefore(EURO_ADOPTION);
        };
    }

    private static Optional<BigDecimal> parse(String text) {
        String compact = text.replace(" ", "").toLowerCase(Locale.ROOT);
        if (EXCEL_NUMBER.matcher(compact).matches()) {
            return Optional.of(new BigDecimal(compact));
        }
        if (!isPlainAmount(text)) {
            return Optional.empty();
        }
        // The cell is now known to be "1.500,50" or "1 500,50" with an optional sign and currency, so the number is read
        // directly; AmountParser stopped at the first space and read "1 500,50 kn" as 1 kn.
        String number = CURRENCY_TOKEN.matcher(text.toLowerCase(Locale.ROOT)).replaceAll("")
                .replaceAll("[.\\s\\u00a0]", "").replace(',', '.');
        return Optional.of(new BigDecimal(number));
    }

    /**
     * A cell may hold only a number and a currency marker. Real registers have durations in amount columns when
     * columns are shifted ("3 MJESECA", "30 DANA", Općina Cernik 2022), unit prices ("257,50 kn / tona") and ranges
     * ("30.000,00 kn / 35.000,00 kn"); reading the first number of such text gave amounts like 3 EUR.
     */
    private static boolean isPlainAmount(String text) {
        String rest = CURRENCY_TOKEN.matcher(text.toLowerCase(Locale.ROOT)).replaceAll("");
        return AMOUNT_CHARS.matcher(rest).matches();
    }

    private static final Pattern CURRENCY_TOKEN = Pattern.compile("\\bkuna\\b|\\bkn\\b|\\bhrk\\b|\\beura?\\b|\\be\\b|€");
    /** Thousands groups with dots or spaces, up to two decimals: rejects dates ("28.02.2023") and file numbers ("2178-16-02-22-7"). */
    private static final Pattern AMOUNT_CHARS = Pattern.compile(
            "\\s*-?\\s*(?:\\d{1,3}(?:[.\\s\\u00a0]\\d{3})+|\\d+)(?:,\\d{1,2})?\\s*");
}
