package com.datathon.api.integration.ckan;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class CkanClient {

    public static final String SOURCE = "data.gov.hr";
    private static final String BASE_URL = "https://data.gov.hr/ckan/api/3/action/";
    private static final int MAX_ROWS = 1000;

    private final PoliteHttpClient httpClient;

    public List<CkanPackage> searchPackages(String query) {
        String url = BASE_URL + "package_search?rows=" + MAX_ROWS + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        JsonNode body;
        try {
            body = httpClient.getJson(url);
        } catch (HttpFetchException e) {
            throw new ExternalSourceException(SOURCE, "package_search failed", e);
        }
        if (!body.path("success").asBoolean(false)) {
            throw new ExternalSourceException(SOURCE, "package_search returned success=false");
        }
        List<CkanPackage> packages = new ArrayList<>();
        for (JsonNode node : body.path("result").path("results")) {
            packages.add(toPackage(node));
        }
        return packages;
    }

    private static CkanPackage toPackage(JsonNode node) {
        List<CkanResource> resources = new ArrayList<>();
        for (JsonNode resource : node.path("resources")) {
            resources.add(new CkanResource(
                    resource.path("id").asString(""), resource.path("name").asString(""),
                    resource.path("url").asString(""), resource.path("format").asString(""),
                    parseTime(resource.path("last_modified").asString("")),
                    parseTime(resource.path("created").asString(""))));
        }
        return new CkanPackage(
                node.path("name").asString(""), node.path("title").asString(""),
                node.path("organization").path("title").asString(""), node.path("license_id").asString(""),
                parseTime(node.path("metadata_modified").asString("")), resources);
    }

    private static Instant parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
