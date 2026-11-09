package com.datathon.api.ingest.eojn;

import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.SheetReader.SheetRow;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EojnRegisterParser {

    public record ParsedRow(String locator, Map<String, String> payload, RowOutcome outcome) {
    }

    public record Result(List<ParsedRow> rows, List<String> sheetsWithoutRequiredColumns) {
    }

    private EojnRegisterParser() {
    }

    public static Result parse(List<SheetData> sheets, boolean isSyntheticSource, LocalDate today) {
        List<ParsedRow> rows = new ArrayList<>();
        List<String> incompleteSheets = new ArrayList<>();
        for (SheetData sheet : sheets) {
            Optional<EojnHeader> header = EojnHeader.find(sheet.rows());
            if (header.isEmpty()) {
                continue;
            }
            if (!header.get().hasRequiredColumns()) {
                incompleteSheets.add(sheet.name());
                continue;
            }
            rows.addAll(parseSheet(sheet, header.get(), isSyntheticSource, today));
        }
        return new Result(rows, incompleteSheets);
    }

    private static List<ParsedRow> parseSheet(SheetData sheet, EojnHeader header, boolean isSyntheticSource, LocalDate today) {
        EojnRowParser rowParser = new EojnRowParser(header, isSyntheticSource, today);
        SheetRow headerRow = sheet.rows().get(header.rowIndex());
        List<ParsedRow> parsed = new ArrayList<>();
        for (SheetRow row : sheet.rows().subList(header.rowIndex() + 1, sheet.rows().size())) {
            RowOutcome outcome = rowParser.parse(row);
            if (outcome instanceof RowOutcome.Blank) {
                continue;
            }
            String locator = "sheet=" + sheet.index() + ";row=" + row.rowNumber();
            parsed.add(new ParsedRow(locator, payloadOf(headerRow, row), outcome));
        }
        return parsed;
    }

    private static Map<String, String> payloadOf(SheetRow headerRow, SheetRow row) {
        Map<String, String> payload = new LinkedHashMap<>();
        for (int index = 0; index < row.cells().size(); index++) {
            String name = headerRow.cell(index).isBlank() ? "col_" + (index + 1) : headerRow.cell(index).strip();
            if (!row.cell(index).isBlank()) {
                payload.putIfAbsent(name, row.cell(index));
            }
        }
        return payload;
    }
}
