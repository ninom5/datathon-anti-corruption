package com.datathon.api.assessment.eval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SemicolonCsv {

    private SemicolonCsv() {
    }

    static List<Map<String, String>> parse(String text) {
        List<List<String>> lines = new ArrayList<>();
        for (String line : text.replace("\r", "").replace("﻿", "").split("\n")) {
            if (!line.isBlank()) {
                lines.add(cells(line));
            }
        }
        if (lines.isEmpty()) {
            return List.of();
        }
        List<String> header = lines.getFirst();
        List<Map<String, String>> rows = new ArrayList<>();
        for (List<String> line : lines.subList(1, lines.size())) {
            Map<String, String> row = new LinkedHashMap<>();
            for (int index = 0; index < header.size(); index++) {
                row.put(header.get(index).strip(), index < line.size() ? line.get(index) : "");
            }
            rows.add(row);
        }
        return rows;
    }

    private static List<String> cells(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean isQuoted = false;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (isQuoted && current == '"' && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                cell.append('"');
                index++;
            } else if (current == '"') {
                isQuoted = !isQuoted;
            } else if (current == ';' && !isQuoted) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(current);
            }
        }
        cells.add(cell.toString());
        return cells;
    }
}
