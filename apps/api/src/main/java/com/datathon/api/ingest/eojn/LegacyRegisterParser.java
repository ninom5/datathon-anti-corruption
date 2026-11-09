package com.datathon.api.ingest.eojn;

import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.SheetReader.SheetRow;
import com.datathon.api.ingest.eojn.EojnRegisterParser.ParsedRow;
import com.datathon.api.ingest.normalize.Oibs;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class LegacyRegisterParser {

    public record SheetResult(String sheetName, String buyerName, Oibs.Result buyerOib, List<ParsedRow> rows) {
    }

    private LegacyRegisterParser() {
    }

    public static List<SheetResult> parse(List<SheetData> sheets, String publisherName,
                                          Function<String, Oibs.Result> buyerOibLookup, LocalDate today) {
        List<SheetResult> results = new ArrayList<>();
        for (SheetData sheet : sheets) {
            Optional<LegacyHeader> header = LegacyHeader.find(sheet.rows());
            if (header.isEmpty()) {
                continue;
            }
            String buyerName = header.get().buyerName().orElse(publisherName);
            Oibs.Result buyerOib = buyerOibLookup.apply(buyerName);
            results.add(new SheetResult(sheet.name(), buyerName, buyerOib,
                    parseSheet(sheet, header.get(), buyerName, buyerOib, today)));
        }
        return results;
    }

    private static List<ParsedRow> parseSheet(SheetData sheet, LegacyHeader header, String buyerName,
                                              Oibs.Result buyerOib, LocalDate today) {
        SheetRow headerRow = sheet.rows().get(header.rowIndex());
        LegacyRowParser rowParser = new LegacyRowParser(header, headerRow, buyerName, buyerOib, today);
        List<ParsedRow> parsed = new ArrayList<>();
        for (SheetRow row : sheet.rows().subList(header.rowIndex() + 1, sheet.rows().size())) {
            if (isColumnNumberRow(row)) {
                continue;
            }
            RowOutcome outcome = rowParser.parse(row);
            if (outcome instanceof RowOutcome.Blank) {
                continue;
            }
            String locator = "sheet=" + sheet.index() + ";row=" + row.rowNumber();
            parsed.add(new ParsedRow(locator, payloadOf(headerRow, row), outcome));
        }
        return parsed;
    }

    private static boolean isColumnNumberRow(SheetRow row) {
        long numbered = row.cells().stream().filter(cell -> cell.matches("\\d{1,2}\\.?")).count();
        long filled = row.cells().stream().filter(cell -> !cell.isBlank()).count();
        return filled >= 5 && numbered == filled;
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
