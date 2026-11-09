package com.datathon.api.ingest;

import java.util.ArrayList;
import java.util.List;

final class CsvSplitter {

    private CsvSplitter() {
    }

    static List<String> splitRecords(String text) {
        List<String> records = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean isQuoted = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '"') {
                isQuoted = !isQuoted;
            }
            if (!isQuoted && (character == '\n' || character == '\r')) {
                if (character == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                records.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(character);
        }
        if (!current.isEmpty()) {
            records.add(current.toString());
        }
        return records;
    }

    static char detectSeparator(List<String> records) {
        String sample = String.join("\n", records.subList(0, Math.min(10, records.size())));
        long semicolons = sample.chars().filter(character -> character == ';').count();
        long commas = sample.chars().filter(character -> character == ',').count();
        return semicolons >= commas ? ';' : ',';
    }

    static List<String> splitFields(String record, char separator) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean isQuoted = false;
        for (int index = 0; index < record.length(); index++) {
            char character = record.charAt(index);
            if (character == '"') {
                boolean isEscapedQuote = isQuoted && index + 1 < record.length() && record.charAt(index + 1) == '"';
                if (isEscapedQuote) {
                    current.append('"');
                    index++;
                } else {
                    isQuoted = !isQuoted;
                }
            } else if (character == separator && !isQuoted) {
                fields.add(current.toString().strip());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        fields.add(current.toString().strip());
        return fields;
    }
}
