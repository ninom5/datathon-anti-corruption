package com.datathon.api.ingest.eojn;

import com.datathon.api.ingest.SheetReader.SheetRow;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record LegacyHeader(int rowIndex, Map<LegacyColumn, Integer> columns, Optional<String> buyerName) {

    public static final int SEARCH_ROWS = 25;
    public static final int MIN_MATCHED_COLUMNS = 10;
    public static final Set<LegacyColumn> REQUIRED = Set.of(
            LegacyColumn.SUPPLIER, LegacyColumn.DATE_SIGNED, LegacyColumn.INITIAL_VALUE, LegacyColumn.SUBJECT);

    private static final Pattern BUYER_LINE = Pattern.compile("^naručitelj\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static Optional<LegacyHeader> find(List<SheetRow> rows) {
        for (int index = 0; index < Math.min(SEARCH_ROWS, rows.size()); index++) {
            Map<LegacyColumn, Integer> columns = match(rows.get(index));
            if (columns.size() >= MIN_MATCHED_COLUMNS && columns.keySet().containsAll(REQUIRED)) {
                return Optional.of(new LegacyHeader(index, columns, findBuyerName(rows.subList(0, index))));
            }
        }
        return Optional.empty();
    }

    public int column(LegacyColumn column) {
        return columns.getOrDefault(column, -1);
    }

    public String headerText(SheetRow headerRow, LegacyColumn column) {
        return headerRow.cell(column(column)).toLowerCase(Locale.ROOT);
    }

    private static Map<LegacyColumn, Integer> match(SheetRow row) {
        Map<LegacyColumn, Integer> columns = new EnumMap<>(LegacyColumn.class);
        for (int index = 0; index < row.cells().size(); index++) {
            int position = index;
            LegacyColumn.fromHeader(row.cell(index)).ifPresent(column -> columns.putIfAbsent(column, position));
        }
        return columns;
    }

    private static Optional<String> findBuyerName(List<SheetRow> titleRows) {
        for (SheetRow row : titleRows) {
            for (String cell : row.cells()) {
                Matcher matcher = BUYER_LINE.matcher(cell.strip());
                if (matcher.matches() && !matcher.group(1).isBlank()) {
                    return Optional.of(matcher.group(1).strip());
                }
            }
        }
        return Optional.empty();
    }
}
