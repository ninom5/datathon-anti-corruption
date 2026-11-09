package com.datathon.api.ingest.synthetic;

import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.SheetReader.SheetRow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

final class CsvWorkbookWriter {

    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private CsvWorkbookWriter() {
    }

    static byte[] toXlsx(SheetData csv) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Registar ugovora");
            CellStyles styles = CellStyles.of(workbook);
            for (SheetRow csvRow : csv.rows()) {
                Row row = sheet.createRow(csvRow.rowNumber() - 1);
                for (int column = 0; column < csvRow.cells().size(); column++) {
                    writeCell(row.createCell(column), csvRow.cell(column), styles);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeCell(Cell cell, String text, CellStyles styles) {
        if (NUMBER.matcher(text).matches() && !looksLikeIdentifier(text)) {
            cell.setCellValue(Double.parseDouble(text));
            cell.setCellStyle(styles.amount());
        } else if (ISO_DATE.matcher(text).matches()) {
            cell.setCellValue(LocalDate.parse(text));
            cell.setCellStyle(styles.date());
        } else {
            cell.setCellValue(text);
        }
    }

    private static boolean looksLikeIdentifier(String text) {
        return text.startsWith("0") && text.length() > 1 && !text.startsWith("0.");
    }

    private record CellStyles(CellStyle amount, CellStyle date) {

        static CellStyles of(Workbook workbook) {
            return new CellStyles(style(workbook, "#,##0.00"), style(workbook, "dd.mm.yyyy"));
        }

        private static CellStyle style(Workbook workbook, String format) {
            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.createDataFormat().getFormat(format));
            return style;
        }
    }
}
