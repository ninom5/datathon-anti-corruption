package com.datathon.api.ingest;

import com.datathon.api.ingest.SheetReader.SheetData;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.integration.ckan.CkanPackage;
import com.datathon.api.integration.ckan.CkanResource;
import com.datathon.api.source.FormatClass;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Downloads one CKAN resource, records it as a {@link SourceFile} and classifies it (A/B/C/D). */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStager {

    /** Formats we can open; the rest (PDF, DOCX, HTML, JSON, XML) is counted but not downloaded in the POC. */
    public static final List<String> READABLE_FORMATS = List.of("XLSX", "XLS", "CSV");
    private static final Pattern HTTP_STATUS = Pattern.compile("HTTP (\\d{3})");

    /** {@code sheets} is filled only for class A and only when the file is new (not imported in an earlier run). */
    public record StagedFile(SourceFile file, FormatClass formatClass, boolean isNew, List<SheetData> sheets) {
    }

    private final PoliteHttpClient httpClient;
    private final SourceDatasetRepository datasetRepository;
    private final SourceFileRepository fileRepository;
    private final RawRecordRepository rawRecordRepository;

    public static Optional<String> readableFormat(CkanResource resource) {
        String format = resource.format() == null ? "" : resource.format().strip().toUpperCase(Locale.ROOT);
        if (READABLE_FORMATS.contains(format)) {
            return Optional.of(format);
        }
        String path = resource.url().toLowerCase(Locale.ROOT).split("\\?", 2)[0];
        return READABLE_FORMATS.stream().filter(candidate -> path.endsWith("." + candidate.toLowerCase(Locale.ROOT))).findFirst();
    }

    @Transactional
    public StagedFile stage(Long runId, CkanPackage ckanPackage, CkanResource resource, String format) {
        SourceDataset dataset = upsertDataset(ckanPackage);
        if (!isAbsoluteHttpUrl(resource.url())) {
            return new StagedFile(saveFailed(runId, dataset, resource, format, "Neispravan URL resursa"), null, true, List.of());
        }
        PoliteHttpClient.Fetched fetched;
        try {
            fetched = httpClient.getBinary(resource.url());
        } catch (HttpFetchException | RobotsBlockedException | IllegalArgumentException e) {
            return new StagedFile(saveFailed(runId, dataset, resource, format, e.getMessage()), null, true, List.of());
        }
        String sha256 = sha256(fetched.body());
        Optional<SourceFile> known = fileRepository.findFirstByUrlAndSha256(resource.url(), sha256);
        if (known.isPresent() && rawRecordRepository.existsByFile(known.get())) {
            return new StagedFile(known.get(), known.get().getFormatClass(), false, List.of());
        }
        // A file staged by an interrupted run but never imported is classified and imported again.
        SourceFile file = known.orElseGet(() -> newFile(runId, dataset, resource, format, fetched.retrievedAt()));
        file.setSha256(sha256);
        file.setBytes((long) fetched.body().length);
        file.setHttpStatus(200);
        return classify(file, format, fetched.body());
    }

    private StagedFile classify(SourceFile file, String format, byte[] body) {
        List<SheetData> sheets;
        try {
            sheets = format.equals("CSV") ? SheetReader.readCsv(body) : SheetReader.readWorkbook(body);
        } catch (IOException | RuntimeException e) {
            // POI throws unchecked exceptions for corrupt or mislabelled files (e.g. HTML saved as .xlsx).
            file.setErrorMessage("Datoteka nije čitljiva kao " + format + ": " + e.getClass().getSimpleName());
            fileRepository.save(file);
            return new StagedFile(file, null, true, List.of());
        }
        FormatClass formatClass = FormatClassifier.classify(sheets);
        file.setFormatClass(formatClass);
        fileRepository.save(file);
        return new StagedFile(file, formatClass, true, formatClass == FormatClass.A ? sheets : List.of());
    }

    private SourceDataset upsertDataset(CkanPackage ckanPackage) {
        SourceDataset dataset = datasetRepository
                .findByPortalAndExternalId(SourceDataset.PORTAL_DATA_GOV_HR, ckanPackage.name())
                .orElseGet(() -> new SourceDataset(SourceDataset.PORTAL_DATA_GOV_HR, ckanPackage.name()));
        dataset.setTitle(ckanPackage.title());
        dataset.setPublisher(ckanPackage.organizationTitle());
        dataset.setLicenseId(ckanPackage.licenseId());
        dataset.setLandingUrl(ckanPackage.landingUrl());
        dataset.setMetadataModified(ckanPackage.metadataModified());
        return datasetRepository.save(dataset);
    }

    private SourceFile saveFailed(Long runId, SourceDataset dataset, CkanResource resource, String format, String message) {
        log.warn("Download failed: {} ({})", resource.url(), message);
        SourceFile file = newFile(runId, dataset, resource, format, Instant.now());
        Matcher status = HTTP_STATUS.matcher(message == null ? "" : message);
        file.setHttpStatus(status.find() ? Integer.valueOf(status.group(1)) : null);
        file.setErrorMessage(message);
        return fileRepository.save(file);
    }

    private static SourceFile newFile(Long runId, SourceDataset dataset, CkanResource resource, String format, Instant retrievedAt) {
        SourceFile file = new SourceFile(dataset, resource.url(), retrievedAt);
        file.setResourceId(resource.id());
        file.setName(resource.name());
        file.setResourceModified(resource.modifiedOrCreated());
        file.setFormat(format);
        file.setIngestRunId(runId);
        return file;
    }

    /** CKAN sometimes lists an empty or relative URL (an upload that never finished). */
    private static boolean isAbsoluteHttpUrl(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url.strip().replace(" ", "%20"));
            return uri.getHost() != null && ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()));
        } catch (IllegalArgumentException e) {
            return false;
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
