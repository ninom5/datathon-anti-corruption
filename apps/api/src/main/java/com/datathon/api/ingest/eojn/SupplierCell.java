package com.datathon.api.ingest.eojn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits the A2 cell "Naziv i OIB ugovaratelja" into name and OIB. Real cells (2026-11-03, 4.477 rows):
 * "Crodux plin d.o.o. 50388109754", "HRVATSKI TELEKOM d.d.\nOIB: 81793146560", consortia with several OIBs
 * ("Zajednica ponuditelja: PLAN 10 d.o.o. OIB 31686301517; I.F. PROJEKT d.o.o. 03470041875"), foreign VAT numbers
 * ("SI82719241") and mistyped OIBs with 8 or 12 digits.
 *
 * @param name       the cell without OIBs and the "OIB:" label; null when nothing is left
 * @param oibText    the first 11-digit number (the consortium leader), else the first other digit run, else null
 * @param oibCount   how many distinct 11-digit numbers the cell holds; more than one means a consortium
 */
public record SupplierCell(String name, String oibText, int oibCount) {

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("(?<![\\dA-Za-z])(\\d{11})(?!\\d)");
    /** A run that looks like a mistyped OIB; shorter numbers ("7", "PLAN 10") stay part of the name. */
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
