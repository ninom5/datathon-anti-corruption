package com.datathon.api.ingest;

import com.datathon.api.ingest.FileStager.StagedFile;
import com.datathon.api.integration.ckan.CkanClient;
import com.datathon.api.integration.ckan.CkanPackage;
import com.datathon.api.integration.ckan.CkanResource;
import com.datathon.api.reference.ReferenceDataService;
import com.datathon.api.rules.RulesService;
import com.datathon.api.source.FormatClass;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * One ingest run (docs/ARCHITECTURE.md §4): CKAN discovery, download and classification of every readable
 * "registar ugovora" resource, then import of class A and then A2 files from oldest to newest so that the newest
 * file wins on duplicate contracts, then the contract rules. Reference data (JPT, DZS) is refreshed first. Started from the admin endpoint, never on application start-up.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngestService {

    private final CkanClient ckanClient;
    private final FileStager fileStager;
    private final RegisterImporter registerImporter;
    private final IngestRunRepository runRepository;
    private final IngestIssueRepository issueRepository;
    private final IngestProperties properties;
    private final JsonMapper jsonMapper;
    private final RulesService rulesService;
    private final ReferenceDataService referenceDataService;

    public IngestRun run() {
        IngestRun run = runRepository.save(IngestRun.start());
        try {
            Map<String, Object> stats = execute(run.getId());
            run.setStats(jsonMapper.writeValueAsString(stats));
            run.setStatus(IngestRun.FINISHED);
        } catch (RuntimeException e) {
            log.error("Ingest run {} failed", run.getId(), e);
            run.setStatus(IngestRun.FAILED);
            run.setStats(jsonMapper.writeValueAsString(Map.of("error", e.getClass().getSimpleName())));
        }
        run.setFinishedAt(Instant.now());
        return runRepository.save(run);
    }

    private Map<String, Object> execute(Long runId) {
        Map<String, Object> referenceStats = referenceDataService.refresh(runId);
        List<CkanPackage> packages = ckanClient.searchPackages(properties.query());
        Map<String, Integer> skippedFormats = new TreeMap<>();
        List<StagedFile> staged = stageAll(runId, packages, skippedFormats);
        ImportStats importStats = importParsed(runId, staged, FormatClass.A);
        // A2 after A: an A2 row is skipped when the buyer has class A data for the same year.
        ImportStats legacyStats = importParsed(runId, staged, FormatClass.A2);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("reference", referenceStats);
        stats.put("packages", packages.size());
        stats.put("filesStaged", staged.size());
        stats.put("filesByClass", countByClass(staged));
        stats.put("notDownloadedByFormat", skippedFormats);
        stats.put("rows", importStats.getRows());
        stats.put("contractsCreated", importStats.getCreated());
        stats.put("contractsSuperseded", importStats.getSuperseded());
        stats.put("rejectedByCode", importStats.getRejectedByCode());
        stats.put("warningsByCode", importStats.getWarningsByCode());
        stats.put("legacyA2", legacyStatsOf(legacyStats));
        stats.put("organizationsEnrichedFromJpt", referenceDataService.enrichOrganizations());
        stats.put("indicatorsByRule", rulesService.runRules());
        log.info("Ingest run {}: {}", runId, stats);
        return stats;
    }

    private List<StagedFile> stageAll(Long runId, List<CkanPackage> packages, Map<String, Integer> skippedFormats) {
        List<StagedFile> staged = new ArrayList<>();
        for (CkanPackage ckanPackage : packages) {
            for (CkanResource resource : ckanPackage.resources()) {
                Optional<String> format = FileStager.readableFormat(resource);
                if (format.isEmpty()) {
                    skippedFormats.merge(resource.format() == null || resource.format().isBlank() ? "(bez formata)" : resource.format(), 1, Integer::sum);
                    continue;
                }
                stageOne(runId, ckanPackage, resource, format.get()).ifPresent(staged::add);
            }
        }
        return staged;
    }

    /** One broken resource must not stop the run (skill rule: log, skip, continue). */
    private Optional<StagedFile> stageOne(Long runId, CkanPackage ckanPackage, CkanResource resource, String format) {
        try {
            return Optional.of(fileStager.stage(runId, ckanPackage, resource, format));
        } catch (RuntimeException e) {
            log.error("Staging of {} failed", resource.url(), e);
            return Optional.empty();
        }
    }

    private static Map<String, Object> legacyStatsOf(ImportStats legacy) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("rows", legacy.getRows());
        stats.put("contractsCreated", legacy.getCreated());
        stats.put("contractsCreatedByYear", legacy.getCreatedByYear());
        stats.put("contractsSuperseded", legacy.getSuperseded());
        stats.put("skippedOverlapClassAByYear", legacy.getSkippedOverlapByYear());
        stats.put("rejectedByCode", legacy.getRejectedByCode());
        stats.put("warningsByCode", legacy.getWarningsByCode());
        return stats;
    }

    /** Oldest file first, so that the newest file wins on duplicate contracts. */
    private ImportStats importParsed(Long runId, List<StagedFile> staged, FormatClass formatClass) {
        ImportStats total = new ImportStats();
        staged.stream()
                .filter(file -> file.formatClass() == formatClass && file.isNew())
                .sorted(Comparator.comparing((StagedFile file) -> file.file().getResourceModified(),
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(file -> total.add(importOne(runId, file)));
        return total;
    }

    /** One broken file must not stop the run (skill rule: log, skip, continue). */
    private ImportStats importOne(Long runId, StagedFile file) {
        try {
            ImportStats stats = file.formatClass() == FormatClass.A2
                    ? registerImporter.importLegacy(runId, file.file(), file.sheets())
                    : registerImporter.importClassA(runId, file.file(), file.sheets(), false);
            log.info("Imported {}: {} rows, {} rejected", file.file().getUrl(), stats.getRows(), stats.rejected());
            return stats;
        } catch (RuntimeException e) {
            log.error("Import of {} failed", file.file().getUrl(), e);
            issueRepository.save(new IngestIssue(runId, file.file().getId(), null, IngestIssue.ERROR,
                    IssueCode.UNREADABLE_FILE, "Uvoz datoteke nije uspio: " + e.getClass().getSimpleName()));
            return new ImportStats();
        }
    }

    private static Map<String, Integer> countByClass(List<StagedFile> staged) {
        Map<String, Integer> counts = new TreeMap<>();
        for (StagedFile file : staged) {
            String key = file.formatClass() != null ? file.formatClass().name()
                    : file.file().getHttpStatus() == null || file.file().getHttpStatus() != 200 ? "download_failed" : "unreadable";
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }
}
