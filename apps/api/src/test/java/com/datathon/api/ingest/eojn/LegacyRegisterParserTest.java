package com.datathon.api.ingest.eojn;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.ingest.FormatClassifier;
import com.datathon.api.ingest.IssueCode;
import com.datathon.api.ingest.SheetReader;
import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.eojn.EojnRegisterParser.ParsedRow;
import com.datathon.api.ingest.eojn.LegacyRegisterParser.SheetResult;
import com.datathon.api.ingest.normalize.Oibs;
import com.datathon.api.source.FormatClass;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** Old EOJN register report (A2) on two real files downloaded from data.gov.hr on 2026-11-02. */
class LegacyRegisterParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 3);
    private static final Function<String, Oibs.Result> NO_JPT = name -> new Oibs.Result(null, Oibs.Status.MISSING);

    /** EOJN "RPT_Ugovor" export: title block with "Naručitelj: OPĆINA BLATO", header in row 14, plain numbers in HRK. */
    @Test
    void parsesEojnReportWithBuyerInTitle() throws IOException {
        List<SheetData> sheets = readFixture("opcina-blato-registar-ugovora-2021.xlsx");
        assertThat(FormatClassifier.classify(sheets)).isEqualTo(FormatClass.A2);

        List<SheetResult> result = LegacyRegisterParser.parse(sheets, "Općina Blato", name -> oibOf(name), TODAY);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().buyerName()).isEqualTo("OPĆINA BLATO");
        assertThat(result.getFirst().buyerOib().oib()).isEqualTo("40097918961");
        List<ParsedRow> rows = result.getFirst().rows();
        assertThat(rows).isNotEmpty().allMatch(row -> row.outcome() instanceof RowOutcome.Parsed);
        assertThat(rows.getFirst().locator()).isEqualTo("sheet=1;row=15");
        ParsedContract first = contract(rows.getFirst());
        assertThat(first.contractUrl()).isNull();
        assertThat(first.buyerName()).isEqualTo("OPĆINA BLATO");
        assertThat(first.supplierName()).isEqualTo("INC d.o.o.");
        assertThat(first.supplierOib()).isEqualTo(new Oibs.Result("32652482960", Oibs.Status.VALID));
        assertThat(first.dateSigned()).isEqualTo(LocalDate.of(2021, 3, 2));
        assertThat(first.cpv()).isEqualTo("39294000");
        assertThat(first.currencyOriginal()).isEqualTo("HRK");
        assertThat(first.initialValue()).isEqualByComparingTo("4828.77");      // 36.382,40 kn
        assertThat(first.initialValueGross()).isEqualByComparingTo("5952.19"); // 44.846,75 kn
        assertThat(first.euFunded()).isTrue();
        assertThat(first.paymentNote()).as("cell holds only a line break").isNull();
        assertThat(first.amendmentsValue()).as("A2 has no amendment column").isNull();
        assertThat(first.totalValue()).isNull();
        assertThat(first.qualityFlags()).isEmpty();
    }

    /** Retyped yearly sheets: buyer only in the dataset publisher, currency in the header "(kn)" / "(EUR)". */
    @Test
    void readsCurrencyFromHeaderAndBuyerFromPublisher() throws IOException {
        List<SheetData> sheets = readFixture("hera-registar-ugovora.xlsx");
        assertThat(FormatClassifier.classify(sheets)).isEqualTo(FormatClass.A2);

        List<SheetResult> result = LegacyRegisterParser.parse(sheets, "Hrvatska energetska regulatorna agencija", NO_JPT, TODAY);

        assertThat(result).extracting(SheetResult::sheetName)
                .containsExactly("2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025");
        assertThat(result).allMatch(sheet -> sheet.buyerName().equals("Hrvatska energetska regulatorna agencija"));
        assertThat(result).flatExtracting(SheetResult::rows)
                .as("the '1 2 3 ...' column numbering row is not a contract")
                .noneMatch(row -> row.outcome() instanceof RowOutcome.Rejected rejected
                        && rejected.code().equals(IssueCode.MISSING_DATE_SIGNED) && row.payload().containsValue("2"));

        ParsedContract kuna = contract(sheet(result, "2022").rows().getFirst());
        assertThat(kuna.supplierName()).isEqualTo("POLIKLINIKA LAB PLUS 2");
        assertThat(kuna.supplierOib().oib()).isEqualTo("05355685913");
        assertThat(kuna.dateSigned()).isEqualTo(LocalDate.of(2022, 1, 21));
        assertThat(kuna.currencyOriginal()).isEqualTo("HRK");
        assertThat(kuna.initialValue()).isEqualByComparingTo("5275.73"); // 39.750 kn
        assertThat(kuna.paidAmount()).isEqualByComparingTo("607.21");    // 4.575 kn

        ParsedContract euro = contract(sheet(result, "2023").rows().getFirst());
        assertThat(euro.currencyOriginal()).isEqualTo("EUR");
        assertThat(euro.initialValue()).isEqualByComparingTo("3177.38");
    }

    private static Oibs.Result oibOf(String name) {
        return name.equals("OPĆINA BLATO") ? new Oibs.Result("40097918961", Oibs.Status.VALID) : NO_JPT.apply(name);
    }

    private static SheetResult sheet(List<SheetResult> result, String name) {
        return result.stream().filter(sheet -> sheet.sheetName().equals(name)).findFirst().orElseThrow();
    }

    private static ParsedContract contract(ParsedRow row) {
        return ((RowOutcome.Parsed) row.outcome()).contract();
    }

    private static List<SheetData> readFixture(String name) throws IOException {
        try (InputStream input = LegacyRegisterParserTest.class.getResourceAsStream("/fixtures/eojn-legacy/" + name)) {
            return SheetReader.readWorkbook(input.readAllBytes());
        }
    }
}
