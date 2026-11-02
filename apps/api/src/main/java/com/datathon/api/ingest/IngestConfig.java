package com.datathon.api.ingest;

import com.datathon.api.ingest.http.DiskCache;
import com.datathon.api.ingest.http.PoliteHttpClient;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

// Origin: pattern copied from repo Desktop/datathon, ingest/IngestConfig.java.
@Configuration
@EnableConfigurationProperties(IngestProperties.class)
public class IngestConfig {

    /** At most one request per second per host, three attempts with growing pauses on 429/5xx. */
    @Bean
    public PoliteHttpClient politeHttpClient(IngestProperties properties, JsonMapper jsonMapper) {
        DiskCache cache = new DiskCache(Path.of(properties.cacheDir()));
        return new PoliteHttpClient(cache, jsonMapper, Duration.ofSeconds(1), Duration.ofSeconds(3));
    }
}
