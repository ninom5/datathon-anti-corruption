package com.datathon.api.reference;

import com.datathon.api.ingest.IngestIssue;
import com.datathon.api.ingest.IngestIssueRepository;
import com.datathon.api.ingest.IssueCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceDataService {

    private final JptImporter jptImporter;
    private final MunicipalityImporter municipalityImporter;
    private final JptBodyRepository jptBodyRepository;
    private final IngestIssueRepository issueRepository;

    public Map<String, Object> refresh(Long runId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("jpt", safely(runId, "JPT", () -> jptImporter.importRegister(runId)));
        stats.put("dzs", safely(runId, "DZS", () -> municipalityImporter.importCensus(runId)));
        return stats;
    }

    @Transactional
    public int enrichOrganizations() {
        return jptBodyRepository.enrichOrganizations();
    }

    private Map<String, Object> safely(Long runId, String source, Supplier<Map<String, Object>> step) {
        try {
            return step.get();
        } catch (RuntimeException e) {
            log.error("Reference source {} failed", source, e);
            issueRepository.save(new IngestIssue(runId, null, null, IngestIssue.ERROR, IssueCode.REFERENCE_SOURCE_FAILED,
                    source + ": " + e.getClass().getSimpleName()));
            return Map.of("error", e.getClass().getSimpleName());
        }
    }
}
