package com.datathon.api.source.dto;

import com.datathon.api.source.Locators;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceFile;
import java.math.BigDecimal;
import java.time.Instant;

/** Where a contract row came from: dataset, publisher, file, row, retrieval time, extraction method and confidence. */
public record ProvenanceResponse(
        String datasetTitle,
        String publisher,
        String datasetUrl,
        String licenseId,
        String fileName,
        String fileUrl,
        String locator,
        String locatorLabel,
        Instant retrievedAt,
        String method,
        String extractorVersion,
        BigDecimal confidence,
        boolean synthetic) {

    public static ProvenanceResponse from(Provenance provenance) {
        RawRecord raw = provenance.getRawRecord();
        SourceFile file = raw == null ? null : raw.getFile();
        SourceDataset dataset = file == null ? null : file.getDataset();
        return new ProvenanceResponse(
                dataset == null ? null : dataset.getTitle(),
                dataset == null ? null : dataset.getPublisher(),
                dataset == null ? null : dataset.getLandingUrl(),
                dataset == null ? null : dataset.getLicenseId(),
                file == null ? null : file.getName(),
                file == null ? null : file.getUrl(),
                raw == null ? null : raw.getLocator(),
                raw == null ? null : Locators.humanReadable(raw.getLocator()),
                file == null ? null : file.getRetrievedAt(),
                provenance.getExtractionMethod().code(),
                provenance.getExtractorVersion(),
                provenance.getConfidence(),
                dataset != null && dataset.isSynthetic());
    }
}
