package com.datathon.api.ingest;

import com.datathon.api.contract.ContractRepository;
import com.datathon.api.contract.ContractWriter;
import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.eojn.EojnRegisterParser;
import com.datathon.api.ingest.eojn.EojnRegisterParser.ParsedRow;
import com.datathon.api.ingest.eojn.EojnRowParser;
import com.datathon.api.ingest.eojn.LegacyRegisterParser;
import com.datathon.api.ingest.eojn.LegacyRowParser;
import com.datathon.api.ingest.eojn.ParsedContract;
import com.datathon.api.ingest.normalize.NameNormalizer;
import com.datathon.api.ingest.normalize.Oibs;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationRepository;
import com.datathon.api.reference.JptDirectory;
import com.datathon.api.ingest.eojn.RowOutcome;
import com.datathon.api.source.ExtractionMethod;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.ProvenanceRepository;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceFile;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class RegisterImporter {

    private final RawRecordRepository rawRecordRepository;
    private final ProvenanceRepository provenanceRepository;
    private final IngestIssueRepository issueRepository;
    private final ContractWriter contractWriter;
    private final ContractRepository contractRepository;
    private final JptDirectory jptDirectory;
    private final OrganizationRepository organizationRepository;
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

    @Transactional
    public ImportStats importLegacy(Long runId, SourceFile file, List<SheetData> sheets) {
        ImportStats stats = new ImportStats();
        String publisher = file.getDataset().getPublisher();
        List<LegacyRegisterParser.SheetResult> results =
                LegacyRegisterParser.parse(sheets, publisher, this::findBuyerOib, LocalDate.now());
        Map<Integer, Boolean> overlapByYear = new HashMap<>();
        for (LegacyRegisterParser.SheetResult sheet : results) {
            if (!sheet.buyerOib().isUsable()) {
                issueRepository.save(new IngestIssue(runId, file.getId(), null, IngestIssue.WARNING,
                        IssueCode.BUYER_NOT_IN_JPT, "Naručitelj '" + sheet.buyerName() + "' nije jednoznačno pronađen u JPT-u."));
            }
            for (ParsedRow row : sheet.rows()) {
                importLegacyRow(runId, file, row, overlapByYear, stats);
            }
        }
        return stats;
    }

    private Oibs.Result findBuyerOib(String name) {
        Oibs.Result fromJpt = jptDirectory.findOibByName(name);
        if (fromJpt.isUsable()) {
            return fromJpt;
        }
        List<Organization> known = organizationRepository.findByNameNormAndOibIsNotNullAndIsSyntheticFalse(NameNormalizer.normalize(name));
        return known.size() == 1 ? Oibs.read(known.getFirst().getOib(), false) : fromJpt;
    }

    private void importLegacyRow(Long runId, SourceFile file, ParsedRow row, Map<Integer, Boolean> overlapByYear,
                                 ImportStats stats) {
        if (!(row.outcome() instanceof RowOutcome.Parsed parsed)) {
            importRow(runId, file, row, false, stats);
            return;
        }
        ParsedContract contract = parsed.contract();
        int year = contract.dateSigned().getYear();
        boolean overlaps = overlapByYear.computeIfAbsent(year, y -> contractRepository.existsClassAForBuyerBetween(
                contract.buyerOib().oib(), NameNormalizer.normalize(contract.buyerName()),
                LocalDate.of(y, 1, 1), LocalDate.of(y, 12, 31)));
        if (!overlaps) {
            importRow(runId, file, row, false, stats);
            return;
        }
        RawRecord raw = rawRecordRepository.save(new RawRecord(file, row.locator(), jsonMapper.writeValueAsString(row.payload())));
        stats.countRow();
        issueRepository.save(new IngestIssue(runId, file.getId(), raw.getId(), IngestIssue.WARNING, IssueCode.OVERLAPS_CLASS_A,
                "Naručitelj za " + year + ". ima podatke u standardnom EOJN izvozu; redak nije uvezen."));
        stats.countSkippedOverlap(year);
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
        boolean isLegacy = parsed.contract().contractUrl() == null;
        Provenance provenance = provenanceRepository.save(new Provenance(raw, ExtractionMethod.PARSER,
                isLegacy ? LegacyRowParser.EXTRACTOR_VERSION : EojnRowParser.EXTRACTOR_VERSION, BigDecimal.ONE));
        for (String flag : parsed.contract().qualityFlags()) {
            issueRepository.save(new IngestIssue(runId, file.getId(), raw.getId(), IngestIssue.WARNING, flag, null));
            stats.countWarning(flag);
        }
        String naturalKey = isLegacy ? legacyNaturalKey(parsed.contract()) : parsed.contract().contractUrl();
        ContractWriter.Written written = contractWriter.upsert(naturalKey, parsed.contract(), provenance, isSynthetic);
        if (written.outcome() == ContractWriter.Outcome.CREATED) {
            stats.countCreated(parsed.contract().dateSigned().getYear());
            return;
        }
        Long olderRawId = written.supersededProvenance().map(old -> old.getRawRecord().getId()).orElse(null);
        issueRepository.save(new IngestIssue(runId, file.getId(), olderRawId, IngestIssue.WARNING,
                IssueCode.SUPERSEDED_BY_NEWER_FILE, "Zamijenjeno retkom " + row(raw)));
        stats.countSuperseded();
    }

    static String legacyNaturalKey(ParsedContract contract) {
        String buyer = contract.buyerOib().isUsable() ? contract.buyerOib().oib() : NameNormalizer.normalize(contract.buyerName());
        String supplier = contract.supplierOib().isUsable() ? contract.supplierOib().oib() : NameNormalizer.normalize(contract.supplierName());
        BigDecimal amount = contract.initialValue() != null ? contract.initialValue() : contract.initialValueGross();
        String key = String.join("|", buyer, supplier, contract.dateSigned().toString(),
                amount == null ? "" : amount.toPlainString(), NameNormalizer.normalize(contract.subject()));
        return "eojn-a2:" + FileStager.sha256(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String row(RawRecord raw) {
        return raw.getLocator() + " (raw_record " + raw.getId() + ")";
    }
}
