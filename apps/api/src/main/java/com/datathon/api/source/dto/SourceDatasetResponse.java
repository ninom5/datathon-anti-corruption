package com.datathon.api.source.dto;

import com.datathon.api.source.SourceDataset;
import java.time.Instant;
import java.util.List;

public record SourceDatasetResponse(Long id, String title, String publisher, String portal, String landingUrl,
                                    String licenseId, Instant metadataModified, long contracts,
                                    List<SourceFileResponse> files, boolean synthetic) {

    public static SourceDatasetResponse from(SourceDataset dataset, List<SourceFileResponse> files) {
        long contracts = files.stream().mapToLong(SourceFileResponse::contracts).sum();
        return new SourceDatasetResponse(dataset.getId(), dataset.getTitle(), dataset.getPublisher(), dataset.getPortal(),
                dataset.getLandingUrl(), dataset.getLicenseId(), dataset.getMetadataModified(), contracts, files,
                dataset.isSynthetic());
    }
}
