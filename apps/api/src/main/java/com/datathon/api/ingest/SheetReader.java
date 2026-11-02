package com.datathon.api.ingest;

import com.datathon.api.ingest.http.TextDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Turns XLSX/XLS/CSV bytes into rows of cell text, the same way for every format, so parsers and the classifier
 * never touch POI. Numbers keep full precision ("198400.5"), date-formatted cells become ISO dates.
 */
public final class SheetReader {

    public record SheetRow(int rowNumber, List<String> cells) {

        public String cell(int index) {
            return index >= 0 && index < cells.size() ? cells.get(index) : "";
        }

        public boolean isBlank() {
            return cells.stream().allMatch(String::isBlank);
        }
    }

    /** {@code index} is 1-based, as used in the provenance locator. */
    public record SheetData(int index, String name, List<SheetRow> rows) {
    }

    private SheetReader() {
    }

    public static List<SheetData> readWorkbook(byte[] bytes) throws IOException {
        List<SheetData> sheets = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                Sheet sheet = workbook.getSheetAt(index);
                sheets.add(new SheetData(index + 1, sheet.getSheetName(), readRows(sheet)));
            }
        }
        return sheets;
    }

    public static List<SheetData> readCsv(byte[] bytes) {
        String text = TextDecoder.decode(bytes).replace("﻿", "");
        List<String> lines = CsvSplitter.splitRecords(text);
        char separator = CsvSplitter.detectSeparator(lines);
        List<SheetRow> rows = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            rows.add(new SheetRow(index + 1, CsvSplitter.splitFields(lines.get(index), separator)));
        }
        return List.of(new SheetData(1, "csv", rows));
    }

    private static List<SheetRow> readRows(Sheet sheet) {
        List<SheetRow> rows = new ArrayList<>();
        for (Row row : sheet) {
            List<String> cells = new ArrayList<>();
            for (int column = 0; column < row.getLastCellNum(); column++) {
                cells.add(cellText(row.getCell(column)));
            }
            rows.add(new SheetRow(row.getRowNum() + 1, cells));
        }
        return rows;
    }

    private static String cellText(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        return switch (type) {
            case STRING -> cell.getStringCellValue().strip();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
