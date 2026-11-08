package com.datathon.api.ingest.extract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AmountParser {

    public enum Role { TOTAL, MAX, MIN, PER_BENEFICIARY, UNKNOWN }

    public record Amount(Role role, BigDecimal value) {
    }

    public record Amounts(BigDecimal total, BigDecimal min, BigDecimal max) {

        public static final Amounts NONE = new Amounts(null, null, null);
    }

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;
    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(50);
    private static final BigDecimal MIN_TOTAL = BigDecimal.valueOf(1000);
    private static final int CONTEXT_CHARS = 140;

    private static final Pattern AMOUNT = Pattern.compile(
            "(?<![\\d.,])(\\d{1,3}(?:[.\\s]\\d{3})+|\\d+)(?:,(\\d{1,2}))?\\s*"
                    + "(milijuna|milijun|mil\\.|mln\\.?|tisuća)?\\s*(?:eura|eur\\b|€)",
            FLAGS);
    private static final Pattern AMOUNT_WITH_PREFIX = Pattern.compile(
            "(?:€|EUR)\\s*(\\d{1,3}(?:\\.\\d{3})+|\\d+)(?:,(\\d{1,2}))?", FLAGS);
    private static final Pattern PLAIN_NUMBER = Pattern.compile("(\\d{1,3}(?:\\.\\d{3})+|\\d+)(?:,(\\d{1,2}))?");

    private static final List<Map.Entry<Role, Pattern>> CONTEXT = List.of(
            Map.entry(Role.TOTAL, Pattern.compile("ukupn|raspoloživ|alokacij|iznos sredstava|vrijednost (?:poziva|natječaja)"
                    + "|osiguran[ao]? (?:je|su)? ?u|proračun", FLAGS)),
            Map.entry(Role.MAX, Pattern.compile("najviš|maksimaln|najveć|ne može (?:biti )?već|gornj|do iznosa|do najviše", FLAGS)),
            Map.entry(Role.MIN, Pattern.compile("najniž|minimaln|najmanj|ne može (?:biti )?manj|donj", FLAGS)),
            Map.entry(Role.PER_BENEFICIARY, Pattern.compile("po (?:korisniku|prijavitelju|projektu|zahtjevu|osobi|studentu|učeniku"
                    + "|vozilu|kućanstvu)|mjesečn|stipendij[ae] (?:iznosi|u iznosu)|u iznosu od", FLAGS)));

    private AmountParser() {
    }

    public static List<Amount> findAll(String text) {
        List<Amount> amounts = new ArrayList<>();
        if (text == null) {
            return amounts;
        }
        Matcher matcher = AMOUNT.matcher(text);
        while (matcher.find()) {
            Optional<BigDecimal> value = toValue(matcher.group(1), matcher.group(2), matcher.group(3));
            if (value.isEmpty() || value.get().compareTo(MIN_AMOUNT) < 0) {
                continue;
            }
            String context = text.substring(Math.max(0, matcher.start() - CONTEXT_CHARS), matcher.start());
            amounts.add(new Amount(roleFromContext(context), value.get()));
        }
        return amounts;
    }

    public static Amounts pick(List<String> texts) {
        BigDecimal total = null;
        BigDecimal min = null;
        BigDecimal max = null;
        for (String text : texts) {
            for (Amount amount : findAll(text)) {
                switch (amount.role()) {
                    case TOTAL -> total = total == null ? amount.value() : total;
                    case MAX, PER_BENEFICIARY -> max = max == null ? amount.value() : max;
                    case MIN -> min = min == null ? amount.value() : min;
                    case UNKNOWN -> { }
                }
            }
        }
        if (total != null && total.compareTo(MIN_TOTAL) < 0) {
            total = null;
        }
        if (total != null && max != null && max.compareTo(total) > 0) {
            return new Amounts(max, min, total);
        }
        return new Amounts(total, min, max);
    }

    public static Optional<BigDecimal> parseStructured(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        Matcher withCurrency = AMOUNT.matcher(value);
        if (withCurrency.find()) {
            return toValue(withCurrency.group(1), withCurrency.group(2), withCurrency.group(3));
        }
        Matcher withPrefix = AMOUNT_WITH_PREFIX.matcher(value);
        if (withPrefix.find()) {
            return toValue(withPrefix.group(1), withPrefix.group(2), null);
        }
        Matcher plain = PLAIN_NUMBER.matcher(value);
        return plain.find() ? toValue(plain.group(1), plain.group(2), null) : Optional.empty();
    }

    private static Role roleFromContext(String context) {
        Role best = Role.UNKNOWN;
        int bestPosition = -1;
        for (Map.Entry<Role, Pattern> entry : CONTEXT) {
            Matcher matcher = entry.getValue().matcher(context);
            while (matcher.find()) {
                if (matcher.start() > bestPosition) {
                    best = entry.getKey();
                    bestPosition = matcher.start();
                }
            }
        }
        return best;
    }

    private static Optional<BigDecimal> toValue(String number, String decimals, String multiplier) {
        try {
            String digits = number.replaceAll("[.\\s]", "") + (decimals != null ? "." + decimals : "");
            BigDecimal value = new BigDecimal(digits);
            return Optional.of(value.multiply(multiplierOf(multiplier)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static BigDecimal multiplierOf(String multiplier) {
        if (multiplier == null) {
            return BigDecimal.ONE;
        }
        String lower = multiplier.toLowerCase();
        return lower.startsWith("mil") || lower.startsWith("mln") ? BigDecimal.valueOf(1_000_000) : BigDecimal.valueOf(1_000);
    }
}
