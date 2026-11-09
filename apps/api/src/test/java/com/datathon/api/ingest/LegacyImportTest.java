package com.datathon.api.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.TestcontainersConfiguration;
import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.source.FormatClass;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class LegacyImportTest {

    private static final String OSIJEK_OIB = "30050049642";
    private static final String[] HEADER = {"Evidencijski broj nabave", "Predmet nabave", "CPV", "Broj objave iz EOJN RH",
            "Vrsta postupka", "Naziv i OIB ugovaratelja", "Naziv i OIB podugovaratelja", "Datum sklapanja",
            "Oznaka/broj ugovora", "Rok na koji je sklopljen", "Iznos bez PDV-a", "Iznos PDV-a", "Ukupni iznos s PDV-om",
            "Ugovor se financira iz fondova EU", "Datum izvršenja", "Ukupni isplaćeni iznos s PDV-om", "Obrazloženja", "Napomena"};

    @Autowired
    private RegisterImporter registerImporter;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private SourceDatasetRepository datasetRepository;
    @Autowired
    private SourceFileRepository fileRepository;

    @Test
    void skipsYearsCoveredByClassAAndDeduplicatesWithinA2() throws IOException {
        importOsijekClassA();
        List<SheetData> sheets = SheetReader.readWorkbook(legacyWorkbook());
        assertThat(FormatClassifier.classify(sheets)).isEqualTo(FormatClass.A2);

        ImportStats stats = registerImporter.importLegacy(null, newFile("a2-test", "Grad Osijek"), sheets);

        assertThat(stats.getSkippedOverlapByYear()).containsExactly(Map.entry(2025, 1));
        assertThat(stats.getCreated()).isEqualTo(1);
        assertThat(stats.getCreatedByYear()).containsExactly(Map.entry(2021, 1));
        assertThat(stats.getSuperseded()).as("same 2021 contract in the second sheet").isEqualTo(1);
        Contract imported = contractRepository.findAll().stream()
                .filter(contract -> contract.getNaturalKey().startsWith("eojn-a2:")).findFirst().orElseThrow();
        assertThat(imported.getEojnContractUrl()).isNull();
        assertThat(imported.getBuyer().getOib()).isEqualTo(OSIJEK_OIB);
        assertThat(imported.getInitialValue()).isEqualByComparingTo("10000.00");
        assertThat(imported.getCurrencyOriginal()).isEqualTo("HRK");
        assertThat(imported.getSupplier().getOib()).isEqualTo("87311810356");
    }

    private void importOsijekClassA() throws IOException {
        SourceFile file = newFile("a2-test-osijek-class-a", "Grad Osijek");
        try (InputStream input = getClass().getResourceAsStream("/fixtures/eojn/grad-osijek-registar-ugovora.xlsx")) {
            registerImporter.importClassA(null, file, SheetReader.readWorkbook(input.readAllBytes()), false);
        }
    }

    private static byte[] legacyWorkbook() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet first = sheetWithHeader(workbook, "2021");
            row(first, 4, "1/21", "Poštanske usluge", "64110000", "HP - Hrvatska pošta d.d. 87311810356", "15.03.2021.",
                    "75.345,00 HRK", "94.181,25 HRK");
            row(first, 5, "2/25", "Uredski materijal", "30192000", "HP - Hrvatska pošta d.d. 87311810356", "10.02.2025.",
                    "5.000,00 EUR", "6.250,00 EUR");
            Sheet second = sheetWithHeader(workbook, "2022");
            row(second, 4, "1/21", "Poštanske usluge", "64110000", "HP - Hrvatska pošta d.d. 87311810356", "15.03.2021.",
                    "75.345,00 HRK", "94.181,25 HRK");
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static Sheet sheetWithHeader(XSSFWorkbook workbook, String name) {
        Sheet sheet = workbook.createSheet(name);
        sheet.createRow(0).createCell(0).setCellValue("REGISTAR UGOVORA");
        sheet.createRow(1).createCell(0).setCellValue("Naručitelj: GRAD OSIJEK");
        Row header = sheet.createRow(3);
        for (int index = 0; index < HEADER.length; index++) {
            header.createCell(index).setCellValue(HEADER[index]);
        }
        return sheet;
    }

    private static void row(Sheet sheet, int index, String evid, String subject, String cpv, String supplier,
                            String date, String net, String gross) {
        Row row = sheet.createRow(index);
        String[] cells = {evid, subject, cpv, "", "Jednostavna nabava", supplier, "", date, "", "", net, "", gross};
        for (int column = 0; column < cells.length; column++) {
            row.createCell(column).setCellValue(cells[column]);
        }
    }

    private SourceFile newFile(String name, String publisher) {
        SourceDataset dataset = datasetRepository.findByPortalAndExternalId(SourceDataset.PORTAL_DATA_GOV_HR, name)
                .orElseGet(() -> new SourceDataset(SourceDataset.PORTAL_DATA_GOV_HR, name));
        dataset.setPublisher(publisher);
        datasetRepository.save(dataset);
        return fileRepository.save(new SourceFile(dataset, "https://data.gov.hr/test/" + name + "-" + Instant.now().toEpochMilli() + ".xlsx", Instant.now()));
    }
}
