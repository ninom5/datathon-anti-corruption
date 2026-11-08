package com.datathon.api.ingest.extract;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CroatianDates {

    public record DateMatch(int position, LocalDate date) {
    }

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("siječnja", 1), Map.entry("sijecnja", 1), Map.entry("veljače", 2), Map.entry("veljace", 2),
            Map.entry("ožujka", 3), Map.entry("ozujka", 3), Map.entry("travnja", 4), Map.entry("svibnja", 5),
            Map.entry("lipnja", 6), Map.entry("srpnja", 7), Map.entry("kolovoza", 8), Map.entry("rujna", 9),
            Map.entry("listopada", 10), Map.entry("studenoga", 11), Map.entry("studenog", 11),
            Map.entry("prosinca", 12), Map.entry("siječanj", 1), Map.entry("veljača", 2), Map.entry("ožujak", 3),
            Map.entry("travanj", 4), Map.entry("svibanj", 5), Map.entry("lipanj", 6), Map.entry("srpanj", 7),
            Map.entry("kolovoz", 8), Map.entry("rujan", 9), Map.entry("listopad", 10), Map.entry("studeni", 11),
            Map.entry("prosinac", 12));

    private static final Pattern DATE = Pattern.compile(
            "\\b(\\d{1,2})\\.\\s?(?:(\\d{1,2})\\.\\s?|(" + monthAlternatives() + ")\\s+)(\\d{4}|\\d{2})(?!\\d)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);

    private CroatianDates() {
    }

    public static List<DateMatch> findAll(String text) {
        List<DateMatch> dates = new ArrayList<>();
        if (text == null) {
            return dates;
        }
        Matcher matcher = DATE.matcher(text);
        while (matcher.find()) {
            toDate(matcher).ifPresent(date -> dates.add(new DateMatch(matcher.start(), date)));
        }
        return dates;
    }

    public static Optional<LocalDate> findFirst(String text) {
        return findAll(text).stream().findFirst().map(DateMatch::date);
    }

    private static Optional<LocalDate> toDate(Matcher matcher) {
        String monthNumber = matcher.group(2);
        Integer month = monthNumber != null ? Integer.valueOf(monthNumber) : MONTHS.get(matcher.group(3).toLowerCase());
        String yearText = matcher.group(4);
        int year = yearText.length() == 2 ? 2000 + Integer.parseInt(yearText) : Integer.parseInt(yearText);
        try {
            return Optional.of(LocalDate.of(year, month, Integer.parseInt(matcher.group(1))));
        } catch (DateTimeException e) {
            return Optional.empty();
        }
    }

    private static String monthAlternatives() {
        return MONTHS.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.joining("|"));
    }
}
