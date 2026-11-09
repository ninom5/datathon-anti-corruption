package com.datathon.api.reference;

import com.datathon.api.ingest.FileStager;
import com.datathon.api.ingest.IngestIssue;
import com.datathon.api.ingest.IngestIssueRepository;
import com.datathon.api.ingest.IssueCode;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.normalize.NameNormalizer;
import com.datathon.api.reference.DzsCensusReader.CensusRow;
import com.datathon.api.source.ExtractionMethod;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.ProvenanceRepository;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MunicipalityImporter {

    public static final String EXTRACTOR_VERSION = "dzs-popis2021-xlsx-v1";
    static final String JPT_MUNICIPALITY = "Jedinica lokalne i područne (regionalne) samouprave - Općina";
    static final String JPT_CITY = "Jedinica lokalne i područne (regionalne) samouprave - Grad";
    static final String JPT_ZAGREB = "Jedinica lokalne i područne (regionalne) samouprave - Grad Zagreb";

    private final PoliteHttpClient httpClient;
    private final JptBodyRepository jptBodyRepository;
    private final MunicipalityRepository municipalityRepository;
    private final SourceDatasetRepository datasetRepository;
    private final SourceFileRepository fileRepository;
    private final RawRecordRepository rawRecordRepository;
    private final ProvenanceRepository provenanceRepository;
    private final IngestIssueRepository issueRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public Map<String, Object> importCensus(Long runId) {
        PoliteHttpClient.Fetched fetched = httpClient.getBinary(ReferenceSources.DZS_URL);
        String sha256 = FileStager.sha256(fetched.body());
        Map<String, Object> stats = new LinkedHashMap<>();
        Optional<SourceFile> known = fileRepository.findFirstByUrlAndSha256(ReferenceSources.DZS_URL, sha256);
        stats.put("unchangedFile", known.isPresent());
        List<CensusRow> rows = readRows(fetched.body());
        SourceFile file = known.orElseGet(() -> saveFile(runId, fetched, sha256));
        Map<String, JptBody> jls = jlsByKey();
        int matched = 0;
        int unmatched = 0;
        for (CensusRow row : rows) {
            Optional<String> kind = kindOf(row);
            if (kind.isEmpty()) {
                continue;
            }
            String name = nameOf(row, kind.get());
            String locator = "sheet=" + DzsCensusReader.SHEET_NAME + ";row=" + row.rowNumber();
            RawRecord raw = rawRecordRepository.findByFileAndLocator(file, locator).orElseGet(() ->
                    rawRecordRepository.save(new RawRecord(file, locator, jsonMapper.writeValueAsString(row))));
            JptBody body = jls.get(key(row.county(), kind.get(), name));
            if (body == null) {
                unmatched++;
                issueRepository.save(new IngestIssue(runId, file.getId(), raw.getId(), IngestIssue.WARNING,
                        IssueCode.DZS_UNMATCHED, "JLS iz DZS-a nije pronađena u JPT-u: " + row.county() + " / " + name));
                continue;
            }
            saveMunicipality(body, kind.get(), row, raw);
            matched++;
        }
        stats.put("censusRows", rows.size());
        stats.put("matched", matched);
        stats.put("unmatched", unmatched);
        log.info("DZS: {} JLS matched to JPT, {} unmatched", matched, unmatched);
        return stats;
    }

    private static List<CensusRow> readRows(byte[] body) {
        try {
            return DzsCensusReader.read(body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void saveMunicipality(JptBody body, String kind, CensusRow row, RawRecord raw) {
        Provenance provenance = provenanceRepository.save(
                new Provenance(raw, ExtractionMethod.PARSER, EXTRACTOR_VERSION, BigDecimal.ONE));
        Municipality municipality = municipalityRepository.findById(body.getOib())
                .orElseGet(() -> new Municipality(body.getOib()));
        municipality.setName(body.getName());
        municipality.setKind(kind);
        municipality.setCounty(body.getCounty());
        municipality.setPopulation2021(row.population());
        municipality.setPeerGroup(PeerGroups.of(kind, row.population()));
        municipality.setProvenance(provenance);
        municipalityRepository.save(municipality);
    }

    static Optional<String> kindOf(CensusRow row) {
        if ("Grad Zagreb".equalsIgnoreCase(row.county())) {
            return row.unitKind() == null ? Optional.of("grad_zagreb") : Optional.empty();
        }
        if (row.unitKind() == null || row.unitName() == null) {
            return Optional.empty();
        }
        return switch (row.unitKind().toLowerCase(Locale.ROOT)) {
            case "grad" -> Optional.of("grad");
            case "općina" -> Optional.of("opcina");
            default -> Optional.empty();
        };
    }

    private static String nameOf(CensusRow row, String kind) {
        return kind.equals("grad_zagreb") ? "Zagreb" : row.unitName();
    }

    private Map<String, JptBody> jlsByKey() {
        Map<String, JptBody> byKey = new HashMap<>();
        for (JptBody body : jptBodyRepository.findByJptTypeIn(List.of(JPT_MUNICIPALITY, JPT_CITY, JPT_ZAGREB))) {
            String kind = body.getJptType().equals(JPT_ZAGREB) ? "grad_zagreb"
                    : body.getJptType().equals(JPT_CITY) ? "grad" : "opcina";
            String name = body.getName().replaceFirst("(?iu)^(grad|općina)\\s+", "");
            String county = kind.equals("grad_zagreb") ? "Grad Zagreb" : body.getCounty();
            byKey.put(key(county, kind, name), body);
        }
        return byKey;
    }

    static String key(String county, String kind, String name) {
        String countyNorm = NameNormalizer.normalize(county).replace("zupanija", "").strip();
        return countyNorm + "|" + kind + "|" + NameNormalizer.normalize(name);
    }

    private SourceFile saveFile(Long runId, PoliteHttpClient.Fetched fetched, String sha256) {
        SourceDataset dataset = datasetRepository
                .findByPortalAndExternalId(ReferenceSources.PORTAL_DZS, ReferenceSources.DZS_DATASET)
                .orElseGet(() -> new SourceDataset(ReferenceSources.PORTAL_DZS, ReferenceSources.DZS_DATASET));
        dataset.setTitle("Popis stanovništva, kućanstava i stanova 2021. – stanovništvo po gradovima/općinama");
        dataset.setPublisher("Državni zavod za statistiku");
        dataset.setLicenseId("open-license");
        dataset.setLandingUrl(ReferenceSources.DZS_LANDING);
        datasetRepository.save(dataset);
        SourceFile file = new SourceFile(dataset, ReferenceSources.DZS_URL, fetched.retrievedAt());
        file.setName("popis_2021-stanovnistvo_po_gradovima_opcinama.xlsx");
        file.setFormat("XLSX");
        file.setSha256(sha256);
        file.setBytes((long) fetched.body().length);
        file.setHttpStatus(200);
        file.setIngestRunId(runId);
        return fileRepository.save(file);
    }
}
