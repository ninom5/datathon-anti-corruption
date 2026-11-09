package com.datathon.api.reference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class DzsCensusReader {

    public static final String SHEET_NAME = "1.";

    public record CensusRow(int rowNumber, String county, String unitKind, String unitName, Integer population) {
    }

    private DzsCensusReader() {
    }

    public static List<CensusRow> read(byte[] workbook) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(workbook))) {
            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    if (SHEET_NAME.equals(sheets.getSheetName())) {
                        return readSheet(sheet, strings, reader.getStylesTable());
                    }
                }
            }
            throw new IOException("DZS radna knjiga nema list '" + SHEET_NAME + "'");
        } catch (OpenXML4JException | SAXException | ParserConfigurationException e) {
            throw new IOException("DZS radna knjiga nije čitljiva", e);
        }
    }

    private static List<CensusRow> readSheet(InputStream sheet, ReadOnlySharedStringsTable strings,
                                             org.apache.poi.xssf.model.StylesTable styles)
            throws IOException, SAXException, ParserConfigurationException {
        RowCollector collector = new RowCollector();
        XMLReader parser = SAXHelper.newXMLReader();
        parser.setContentHandler(new XSSFSheetXMLHandler(styles, null, strings, collector, new org.apache.poi.ss.usermodel.DataFormatter(), false));
        parser.parse(new InputSource(sheet));
        return collector.rows;
    }

    private static final class RowCollector implements XSSFSheetXMLHandler.SheetContentsHandler {

        private static final int HEADER_ROW = 8;
        private final List<CensusRow> rows = new ArrayList<>();
        private final String[] cells = new String[6];
        private int rowNumber;

        @Override
        public void startRow(int rowIndex) {
            rowNumber = rowIndex + 1;
            java.util.Arrays.fill(cells, null);
        }

        @Override
        public void cell(String reference, String value, XSSFComment comment) {
            int column = new CellReference(reference).getCol();
            if (column < cells.length) {
                cells[column] = value == null || value.isBlank() ? null : value.strip();
            }
        }

        @Override
        public void endRow(int rowIndex) {
            if (rowNumber <= HEADER_ROW || cells[0] == null) {
                return;
            }
            parsePopulation(cells[5]).ifPresent(population ->
                    rows.add(new CensusRow(rowNumber, cells[0], cells[1], cells[4], population)));
        }

        private static Optional<Integer> parsePopulation(String text) {
            if (text == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(new BigDecimal(text.replace(".", "").replace(",", "").strip()).intValueExact());
            } catch (NumberFormatException | ArithmeticException e) {
                return Optional.empty();
            }
        }
    }
}
