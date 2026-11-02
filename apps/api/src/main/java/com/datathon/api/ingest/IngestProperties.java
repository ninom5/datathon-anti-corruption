package com.datathon.api.ingest;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param cacheDir disk cache for downloads, so a re-run does not hit data.gov.hr again
 * @param query    CKAN search used for discovery (docs/DATA_SOURCES.md §1.1)
 */
@ConfigurationProperties("javniradar.ingest")
public record IngestProperties(String cacheDir, String query) {
}
