package com.datathon.api.ingest.eojn;

import com.datathon.api.ingest.SheetReader.SheetRow;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record EojnHeader(int rowIndex, Map<EojnColumn, Integer> columns) {

    public static final int SEARCH_ROWS = 15;
    public static final int MIN_MATCHED_COLUMNS = 25;
    public static final Set<EojnColumn> REQUIRED = Set.of(
            EojnColumn.CONTRACT_URL, EojnColumn.BUYER_NAME, EojnColumn.DATE_SIGNED, EojnColumn.INITIAL_VALUE);

    public static Optional<EojnHeader> find(List<SheetRow> rows) {
        for (int index = 0; index < Math.min(SEARCH_ROWS, rows.size()); index++) {
            Map<EojnColumn, Integer> columns = match(rows.get(index));
            if (columns.size() >= MIN_MATCHED_COLUMNS) {
                return Optional.of(new EojnHeader(index, columns));
            }
        }
        return Optional.empty();
    }

    public int column(EojnColumn column) {
        return columns.getOrDefault(column, -1);
    }

    public boolean hasRequiredColumns() {
        return columns.keySet().containsAll(REQUIRED);
    }

    private static Map<EojnColumn, Integer> match(SheetRow row) {
        Map<EojnColumn, Integer> columns = new EnumMap<>(EojnColumn.class);
        for (int index = 0; index < row.cells().size(); index++) {
            int position = index;
            EojnColumn.fromHeader(row.cell(index)).ifPresent(column -> columns.putIfAbsent(column, position));
        }
        return columns;
    }
}
