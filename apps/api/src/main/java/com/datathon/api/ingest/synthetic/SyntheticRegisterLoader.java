package com.datathon.api.ingest.synthetic;

import com.datathon.api.ingest.FormatClassifier;
import com.datathon.api.ingest.ImportStats;
import com.datathon.api.ingest.IngestRun;
import com.datathon.api.ingest.IngestRunRepository;
import com.datathon.api.ingest.RegisterImporter;
import com.datathon.api.ingest.SheetReader;
import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.rules.RulesService;
import com.datathon.api.source.FormatClass;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loads the synthetic demo register (two fictional municipalities) through the same XLSX reader, class A parser and
 * rules as real data, marked {@code is_synthetic} everywhere (AGENTS.md). The expected outcome of every row lives
 * only in the test fixtures, never in production tables.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyntheticRegisterLoader {

    public static final String RESOURCE = "synthetic/registar-ugovora-primjerovo.csv";
    /** Reserved .invalid domain (RFC 2606), so a synthetic link can never open a real contract. */
    public static final String FILE_URL = "https://primjerovo.invalid/registar-ugovora-primjerovo.xlsx";
    private static final String DATASET_ID = "javniradar-sinteticki-registar";

    private final SourceDatasetRepository datasetRepository;
    private final SourceFileRepository fileRepository;
    private final RawRecordRepository rawRecordRepository;
    private final IngestRunRepository runRepository;
    private final RegisterImporter registerImporter;
    private final RulesService rulesService;
    private final JsonMapper jsonMapper;

    /** Idempotent: an unchanged synthetic file is not imported twice; rules are always recomputed. */
    public Map<String, Object> load() {
        byte[] xlsx = CsvWorkbookWriter.toXlsx(SheetReader.readCsv(readResource()).getFirst());
        String sha256 = sha256(xlsx);
        Map<String, Object> stats = new LinkedHashMap<>();
        boolean isKnown = fileRepository.findFirstByUrlAndSha256(FILE_URL, sha256)
                .filter(rawRecordRepository::existsByFile)
                .isPresent();
        if (!isKnown) {
            stats.put("import", importFile(readWorkbook(xlsx), xlsx.length, sha256));
        }
        stats.put("indicatorsByRule", rulesService.runContractRules());
        log.info("Synthetic register: {}", stats);
        return stats;
    }

    private Map<String, Object> importFile(List<SheetData> sheets, int bytes, String sha256) {
        FormatClass formatClass = FormatClassifier.classify(sheets);
        if (formatClass != FormatClass.A) {
            throw new IllegalStateException("Synthetic register must be class A, was " + formatClass);
        }
        IngestRun run = runRepository.save(IngestRun.start());
        SourceFile file = new SourceFile(syntheticDataset(), FILE_URL, Instant.now());
        file.setName("registar-ugovora-primjerovo.xlsx");
        file.setFormat("XLSX");
        file.setFormatClass(formatClass);
        file.setSha256(sha256);
        file.setBytes((long) bytes);
        file.setHttpStatus(200);
        file.setIngestRunId(run.getId());
        file = fileRepository.save(file);
        ImportStats importStats = registerImporter.importClassA(run.getId(), file, sheets, true);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("rows", importStats.getRows());
        stats.put("contractsCreated", importStats.getCreated());
        stats.put("rejectedByCode", importStats.getRejectedByCode());
        stats.put("warningsByCode", importStats.getWarningsByCode());
        run.setStats(jsonMapper.writeValueAsString(stats));
        run.setStatus(IngestRun.FINISHED);
        run.setFinishedAt(Instant.now());
        runRepository.save(run);
        return stats;
    }

    private SourceDataset syntheticDataset() {
        SourceDataset dataset = datasetRepository.findByPortalAndExternalId(SourceDataset.PORTAL_SYNTHETIC, DATASET_ID)
                .orElseGet(() -> new SourceDataset(SourceDataset.PORTAL_SYNTHETIC, DATASET_ID));
        dataset.setTitle("SINTETIČKI PODACI ZA DEMONSTRACIJU – registar ugovora (Općina Primjerovo, Općina Oglednik)");
        dataset.setPublisher("JavniRadar (sintetički skup, nije stvarni izdavatelj)");
        dataset.setSynthetic(true);
        return datasetRepository.save(dataset);
    }

    private static byte[] readResource() {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<SheetData> readWorkbook(byte[] xlsx) {
        try {
            return SheetReader.readWorkbook(xlsx);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sha256(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
