package com.datathon.api.reference;

import com.datathon.api.ingest.FileStager;
import com.datathon.api.ingest.normalize.NameNormalizer;
import com.datathon.api.integration.jpt.JptClient;
import com.datathon.api.integration.jpt.JptEntry;
import com.datathon.api.source.ExtractionMethod;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.ProvenanceRepository;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loads the JPT register into {@code jpt_body}: one raw record and provenance per body, so every OIB, type and
 * founder list can be traced to the API response. An unchanged response (same sha256) is not imported again.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JptImporter {

    public static final String EXTRACTOR_VERSION = "jpt-api-v1";

    private final JptClient jptClient;
    private final SourceDatasetRepository datasetRepository;
    private final SourceFileRepository fileRepository;
    private final RawRecordRepository rawRecordRepository;
    private final ProvenanceRepository provenanceRepository;
    private final JptBodyRepository jptBodyRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public Map<String, Object> importRegister(Long runId) {
        JptClient.Fetched fetched = jptClient.fetchAll();
        String sha256 = FileStager.sha256(fetched.body());
        Optional<SourceFile> known = fileRepository.findFirstByUrlAndSha256(JptClient.LIST_URL, sha256);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("bodies", fetched.entries().size());
        if (known.isPresent() && rawRecordRepository.existsByFile(known.get())) {
            stats.put("unchanged", true);
            return stats;
        }
        SourceFile file = saveFile(runId, fetched, sha256);
        int index = 0;
        int withoutOib = 0;
        for (JptEntry entry : fetched.entries()) {
            index++;
            if (entry.oib() == null || !entry.oib().matches("\\d{11}")) {
                withoutOib++;
                continue;
            }
            saveBody(file, index, entry);
        }
        stats.put("withoutOib", withoutOib);
        log.info("JPT: {} bodies imported, {} without OIB", fetched.entries().size() - withoutOib, withoutOib);
        return stats;
    }

    private void saveBody(SourceFile file, int index, JptEntry entry) {
        RawRecord raw = rawRecordRepository.save(new RawRecord(file, "index=" + index + ";oib=" + entry.oib(), entry.rawJson()));
        Provenance provenance = provenanceRepository.save(
                new Provenance(raw, ExtractionMethod.API, EXTRACTOR_VERSION, BigDecimal.ONE));
        JptBody body = jptBodyRepository.findById(entry.oib()).orElseGet(() -> new JptBody(entry.oib()));
        body.setJptCode(entry.jptCode());
        body.setName(entry.name());
        body.setNameNorm(NameNormalizer.normalize(entry.name()));
        body.setJptType(entry.jptType());
        body.setCounty(entry.county());
        body.setMunicipalityName(entry.municipalityName());
        body.setFoundedOn(entry.foundedOn());
        body.setFounders(foundersJson(entry.founders()));
        body.setProvenance(provenance);
        jptBodyRepository.save(body);
    }

    private String foundersJson(List<JptEntry.Founder> founders) {
        List<Map<String, String>> list = new ArrayList<>();
        for (JptEntry.Founder founder : founders) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("oib", founder.oib());
            item.put("naziv", founder.name());
            list.add(item);
        }
        return jsonMapper.writeValueAsString(list);
    }

    private SourceFile saveFile(Long runId, JptClient.Fetched fetched, String sha256) {
        SourceDataset dataset = datasetRepository
                .findByPortalAndExternalId(ReferenceSources.PORTAL_JPT, ReferenceSources.JPT_DATASET)
                .orElseGet(() -> new SourceDataset(ReferenceSources.PORTAL_JPT, ReferenceSources.JPT_DATASET));
        dataset.setTitle("Popis brojčanih oznaka javnopravnih tijela (JPT)");
        dataset.setPublisher("Ministarstvo pravosuđa, uprave i digitalne transformacije");
        dataset.setLicenseId("open-license");
        dataset.setLandingUrl(ReferenceSources.JPT_LANDING);
        datasetRepository.save(dataset);
        SourceFile file = new SourceFile(dataset, JptClient.LIST_URL, fetched.retrievedAt());
        file.setName("PopisOznakaNaDanasnjiDan");
        file.setFormat("JSON");
        file.setSha256(sha256);
        file.setBytes((long) fetched.body().length);
        file.setHttpStatus(200);
        file.setIngestRunId(runId);
        return fileRepository.save(file);
    }
}
