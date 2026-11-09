package com.datathon.api.ingest.eojn;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.contract.SubjectType;
import com.datathon.api.ingest.FormatClassifier;
import com.datathon.api.ingest.IssueCode;
import com.datathon.api.ingest.SheetReader;
import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.SheetReader.SheetRow;
import com.datathon.api.ingest.normalize.Oibs;
import com.datathon.api.source.FormatClass;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EojnRegisterParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);

    @Test
    void parsesRealOsijekRegister() throws IOException {
        List<SheetData> sheets = readFixture("grad-osijek-registar-ugovora.xlsx");

        EojnRegisterParser.Result result = EojnRegisterParser.parse(sheets, false, TODAY);

        assertThat(FormatClassifier.classify(sheets)).isEqualTo(FormatClass.A);
        assertThat(result.rows()).hasSize(588);
        ParsedContract first = ((RowOutcome.Parsed) result.rows().getFirst().outcome()).contract();
        assertThat(result.rows().getFirst().locator()).isEqualTo("sheet=1;row=2");
        assertThat(first.contractUrl()).isEqualTo("https://eojn.hr/contract-eo/730419");
        assertThat(first.buyerOib()).isEqualTo(new Oibs.Result("30050049642", Oibs.Status.VALID));
        assertThat(first.supplierOib()).isEqualTo(new Oibs.Result("87311810356", Oibs.Status.VALID));
        assertThat(first.dateSigned()).isEqualTo(LocalDate.of(2025, 12, 12));
        assertThat(first.cpv()).isEqualTo("64110000");
        assertThat(first.cpvLabel()).isEqualTo("Poštanske usluge");
        assertThat(first.subjectType()).isEqualTo(SubjectType.SERVICES);
        assertThat(first.initialValue()).isEqualByComparingTo("198400");
        assertThat(first.amendmentsValue()).isEqualByComparingTo("0");
        assertThat(first.paidAmount()).isNull();
        assertThat(first.currencyOriginal()).isEqualTo("EUR");
        assertThat(first.isFrameworkBased()).isFalse();
    }

    @Test
    void marksContractBasedOnFrameworkAgreement() throws IOException {
        List<SheetData> sheets = readFixture("grad-osijek-registar-ugovora.xlsx");

        ParsedContract second = ((RowOutcome.Parsed) EojnRegisterParser.parse(sheets, false, TODAY)
                .rows().get(1).outcome()).contract();

        assertThat(second.frameworkAgreementUrl()).isEqualTo("https://eojn.hr/contract-eo/556322");
        assertThat(second.isFrameworkBased()).isTrue();
    }

    @Test
    void findsHeaderBelowTitleRows() {
        List<SheetRow> rows = new ArrayList<>();
        rows.add(row(1, "REGISTAR UGOVORA OPĆINE PRIMJEROVO"));
        rows.add(row(2, ""));
        rows.add(row(3, headerCells()));
        rows.add(row(4, dataCells("https://primjerovo.invalid/contract-eo/S900", "15.03.2025.", "12.000,00")));

        EojnRegisterParser.Result result = EojnRegisterParser.parse(List.of(new SheetData(1, "List1", rows)), true, TODAY);

        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().getFirst().locator()).isEqualTo("sheet=1;row=4");
        ParsedContract contract = ((RowOutcome.Parsed) result.rows().getFirst().outcome()).contract();
        assertThat(contract.dateSigned()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(contract.initialValue()).isEqualByComparingTo(new BigDecimal("12000.00"));
        assertThat(contract.buyerOib().status()).isEqualTo(Oibs.Status.SYNTHETIC_RESERVED);
    }

    @Test
    void rejectsRowWithUnreadableSigningDate() {
        List<SheetRow> rows = List.of(row(1, headerCells()),
                row(2, dataCells("https://primjerovo.invalid/contract-eo/S901", "32.13.2025.", "12000")));

        RowOutcome outcome = EojnRegisterParser.parse(List.of(new SheetData(1, "List1", rows)), true, TODAY)
                .rows().getFirst().outcome();

        assertThat(outcome).isEqualTo(new RowOutcome.Rejected(IssueCode.UNREADABLE_DATE_SIGNED, "Datum sklapanja nije čitljiv."));
    }

    @Test
    void convertsHrkBeforeEuroAndRejectsRowWithoutAmount() {
        List<SheetRow> rows = List.of(row(1, headerCells()),
                row(2, dataCells("https://primjerovo.invalid/contract-eo/S902", "10.06.2022.", "75.345,00")),
                row(3, dataCells("https://primjerovo.invalid/contract-eo/S903", "10.06.2025.", "")));

        List<EojnRegisterParser.ParsedRow> parsed = EojnRegisterParser.parse(List.of(new SheetData(1, "List1", rows)), true, TODAY).rows();

        ParsedContract hrk = ((RowOutcome.Parsed) parsed.get(0).outcome()).contract();
        assertThat(hrk.initialValue()).isEqualByComparingTo("10000.00");
        assertThat(hrk.currencyOriginal()).isEqualTo("HRK");
        assertThat(parsed.get(1).outcome()).isInstanceOf(RowOutcome.Rejected.class);
        assertThat(((RowOutcome.Rejected) parsed.get(1).outcome()).code()).isEqualTo(IssueCode.MISSING_AMOUNT);
    }

    private static List<SheetData> readFixture(String name) throws IOException {
        try (InputStream input = EojnRegisterParserTest.class.getResourceAsStream("/fixtures/eojn/" + name)) {
            return SheetReader.readWorkbook(input.readAllBytes());
        }
    }

    private static String[] headerCells() {
        return java.util.Arrays.stream(EojnColumn.values()).map(EojnColumn::header).toArray(String[]::new);
    }

    private static String[] dataCells(String url, String dateSigned, String initialValue) {
        String[] cells = new String[EojnColumn.values().length];
        java.util.Arrays.fill(cells, "");
        cells[EojnColumn.CONTRACT_URL.ordinal()] = url;
        cells[EojnColumn.BUYER_NAME.ordinal()] = "Općina Primjerovo";
        cells[EojnColumn.BUYER_OIB.ordinal()] = "00000000001";
        cells[EojnColumn.SUPPLIER_NAME.ordinal()] = "Primjer d.o.o.";
        cells[EojnColumn.SUPPLIER_OIB.ordinal()] = "00000000002";
        cells[EojnColumn.SUBJECT.ordinal()] = "Održavanje nerazvrstanih cesta";
        cells[EojnColumn.CPV.ordinal()] = "45233141 - Radovi na održavanju cesta";
        cells[EojnColumn.DATE_SIGNED.ordinal()] = dateSigned;
        cells[EojnColumn.INITIAL_VALUE.ordinal()] = initialValue;
        return cells;
    }

    private static SheetRow row(int number, String... cells) {
        return new SheetRow(number, List.of(cells));
    }
}
