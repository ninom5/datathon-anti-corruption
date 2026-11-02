package com.datathon.api.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns a raw record locator ({@code sheet=1;row=41}) into text a person can find in the file ("list 1, redak 41"). */
public final class Locators {

    private static final Pattern SHEET_ROW = Pattern.compile("sheet=(\\d+);row=(\\d+)");

    private Locators() {
    }

    public static String humanReadable(String locator) {
        if (locator == null) {
            return null;
        }
        Matcher matcher = SHEET_ROW.matcher(locator);
        return matcher.matches() ? "list " + matcher.group(1) + ", redak " + matcher.group(2) : locator;
    }
}
