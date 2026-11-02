package com.datathon.api.ingest;

import com.datathon.api.contract.ContractWriter;
import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.eojn.EojnRegisterParser;
import com.datathon.api.ingest.eojn.EojnRegisterParser.ParsedRow;
import com.datathon.api.ingest.eojn.EojnRowParser;
import com.datathon.api.ingest.eojn.RowOutcome;
import com.datathon.api.source.ExtractionMethod;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.ProvenanceRepository;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Imports one class A file in one transaction: every row is kept as a raw record, parsed rows become contracts
 * with provenance, rejected rows become ingest issues. Used for real files and the synthetic demo set alike.
 */
@Service
@RequiredArgsConstructor
public class RegisterImporter {

    private final RawRecordRepository rawRecordRepository;
    private final ProvenanceRepository provenanceRepository;
    private final IngestIssueRepository issueRepository;
    private final ContractWriter contractWriter;
    private final JsonMapper jsonMapper;

    @Transactional
    public ImportStats importClassA(Long runId, SourceFile file, List<SheetData> sheets, boolean isSynthetic) {
        ImportStats stats = new ImportStats();
        EojnRegisterParser.Result result = EojnRegisterParser.parse(sheets, isSynthetic, LocalDate.now());
        for (String sheet : result.sheetsWithoutRequiredColumns()) {
            issueRepository.save(new IngestIssue(runId, file.getId(), null, IngestIssue.ERROR,
                    IssueCode.MISSING_REQUIRED_COLUMNS, "List '" + sheet + "' nema obavezne stupce."));
        }
        for (ParsedRow row : result.rows()) {
            importRow(runId, file, row, isSynthetic, stats);
        }
        return stats;
    }

    private void importRow(Long runId, SourceFile file, ParsedRow row, boolean isSynthetic, ImportStats stats) {
        RawRecord raw = rawRecordRepository.save(new RawRecord(file, row.locator(), jsonMapper.writeValueAsString(row.payload())));
        stats.countRow();
        switch (row.outcome()) {
            case RowOutcome.Rejected rejected -> {
                issueRepository.save(new IngestIssue(runId, file.getId(), raw.getId(), IngestIssue.ERROR,
                        rejected.code(), rejected.message()));
                stats.countRejected(rejected.code());
            }
            case RowOutcome.Parsed parsed -> importContract(runId, file, raw, parsed, isSynthetic, stats);
            case RowOutcome.Blank blank -> { }
        }
    }

    private void importContract(Long runId, SourceFile file, RawRecord raw, RowOutcome.Parsed parsed,
                                boolean isSynthetic, ImportStats stats) {
        Provenance provenance = provenanceRepository.save(
                new Provenance(raw, ExtractionMethod.PARSER, EojnRowParser.EXTRACTOR_VERSION, BigDecimal.ONE));
        for (String flag : parsed.contract().qualityFlags()) {
            issueRepository.save(new IngestIssue(runId, file.getId(), raw.getId(), IngestIssue.WARNING, flag, null));
            stats.countWarning(flag);
        }
        ContractWriter.Written written = contractWriter.upsert(parsed.contract(), provenance, isSynthetic);
        if (written.outcome() == ContractWriter.Outcome.CREATED) {
            stats.countCreated();
            return;
        }
        Long olderRawId = written.supersededProvenance().map(old -> old.getRawRecord().getId()).orElse(null);
        issueRepository.save(new IngestIssue(runId, file.getId(), olderRawId, IngestIssue.WARNING,
                IssueCode.SUPERSEDED_BY_NEWER_FILE, "Zamijenjeno retkom " + row(raw)));
        stats.countSuperseded();
    }

    private static String row(RawRecord raw) {
        return raw.getLocator() + " (raw_record " + raw.getId() + ")";
    }
}
