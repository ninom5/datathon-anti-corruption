package com.datathon.api.ingest.eojn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SupplierCell(String name, String oibText, int oibCount) {

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("(?<![\\dA-Za-z])(\\d{11})(?!\\d)");
    private static final Pattern OTHER_DIGIT_RUN = Pattern.compile("(?<![\\dA-Za-z])(\\d{8,13})(?!\\d)");
    private static final Pattern OIB_LABEL = Pattern.compile("\\bOIB\\s*:?", Pattern.CASE_INSENSITIVE);
    private static final Pattern EDGE_PUNCTUATION = Pattern.compile("^[\\s,;:\\-–]+|[\\s,;:\\-–]+$");

    public static SupplierCell split(String cell) {
        if (cell == null || cell.isBlank()) {
            return new SupplierCell(null, null, 0);
        }
        List<String> oibs = distinctMatches(ELEVEN_DIGITS, cell);
        String oibText = oibs.isEmpty() ? firstMatch(OTHER_DIGIT_RUN, cell) : oibs.getFirst();
        String name = ELEVEN_DIGITS.matcher(cell).replaceAll(" ");
        if (oibs.isEmpty() && oibText != null) {
            name = name.replace(oibText, " ");
        }
        name = OIB_LABEL.matcher(name).replaceAll(" ").replaceAll("\\s+", " ");
        name = EDGE_PUNCTUATION.matcher(name).replaceAll("");
        return new SupplierCell(name.isBlank() ? null : name, oibText, oibs.size());
    }

    public boolean isConsortium() {
        return oibCount > 1;
    }

    private static List<String> distinctMatches(Pattern pattern, String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (!found.contains(matcher.group(1))) {
                found.add(matcher.group(1));
            }
        }
        return found;
    }

    private static String firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }
}
