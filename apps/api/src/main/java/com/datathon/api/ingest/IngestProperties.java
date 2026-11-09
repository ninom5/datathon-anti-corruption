package com.datathon.api.ingest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("javniradar.ingest")
public record IngestProperties(String cacheDir, String query) {
}
