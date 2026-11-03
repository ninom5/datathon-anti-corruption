package com.datathon.api.ingest;

import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.SheetReader.SheetRow;
import com.datathon.api.ingest.eojn.EojnHeader;
import com.datathon.api.ingest.eojn.LegacyHeader;
import com.datathon.api.source.FormatClass;
import java.util.List;
import java.util.Locale;

/**
 * Format classes from docs/DATA_SOURCES.md §1.1, decided from the first rows of each sheet:
 * A = EOJN header found; A2 = header of the old EOJN register report; B = a header-like row with an OIB column;
 * C = a header-like row without OIB; D = neither.
 */
public final class FormatClassifier {

    private static final int SAMPLE_ROWS = 20;
    /** A header-like row has at least this many short, non-numeric cells. */
    private static final int MIN_HEADER_CELLS = 4;
    private static final int MAX_HEADER_CELL_LENGTH = 80;

    private FormatClassifier() {
    }

    public static FormatClass classify(List<SheetData> sheets) {
        if (sheets.stream().anyMatch(sheet -> EojnHeader.find(sheet.rows()).isPresent())) {
            return FormatClass.A;
        }
        if (sheets.stream().anyMatch(sheet -> LegacyHeader.find(sheet.rows()).isPresent())) {
            return FormatClass.A2;
        }
        boolean hasHeader = false;
        for (SheetData sheet : sheets) {
            for (SheetRow row : sheet.rows().subList(0, Math.min(SAMPLE_ROWS, sheet.rows().size()))) {
                if (!isHeaderLike(row)) {
                    continue;
                }
                if (row.cells().stream().anyMatch(cell -> cell.toLowerCase(Locale.ROOT).contains("oib"))) {
                    return FormatClass.B;
                }
                hasHeader = true;
            }
        }
        return hasHeader ? FormatClass.C : FormatClass.D;
    }

    private static boolean isHeaderLike(SheetRow row) {
        long textCells = row.cells().stream()
                .filter(cell -> !cell.isBlank() && cell.length() <= MAX_HEADER_CELL_LENGTH && !cell.matches("[\\d.,\\s-]+"))
                .count();
        return textCells >= MIN_HEADER_CELLS;
    }
}
