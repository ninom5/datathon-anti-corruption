package com.datathon.api.integration.jpt;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class JptClient {

    public static final String SOURCE = "jpt.gov.hr";
    public static final String LIST_URL = "https://jpt.gov.hr/Backend_Prod/api/PopisOznakaNaDanasnjiDan";

    private final PoliteHttpClient httpClient;
    private final JsonMapper jsonMapper;

    public record Fetched(byte[] body, Instant retrievedAt, List<JptEntry> entries) {
    }

    public Fetched fetchAll() {
        PoliteHttpClient.Fetched fetched;
        try {
            fetched = httpClient.getBinary(LIST_URL);
        } catch (HttpFetchException | RobotsBlockedException e) {
            throw new ExternalSourceException(SOURCE, "PopisOznakaNaDanasnjiDan failed", e);
        }
        return new Fetched(fetched.body(), fetched.retrievedAt(), parse(fetched.body()));
    }

    public List<JptEntry> parse(byte[] body) {
        String text = new String(body, StandardCharsets.UTF_8).replace("﻿", "");
        JsonNode root = jsonMapper.readTree(text);
        if (!root.isArray()) {
            throw new ExternalSourceException(SOURCE, "expected a JSON array");
        }
        List<JptEntry> entries = new ArrayList<>();
        for (JsonNode node : root) {
            entries.add(toEntry(node));
        }
        return entries;
    }

    private JptEntry toEntry(JsonNode node) {
        List<JptEntry.Founder> founders = new ArrayList<>();
        for (JsonNode founder : node.path("Osnivaci")) {
            founders.add(new JptEntry.Founder(blankToNull(founder.path("OIB").asString("")),
                    blankToNull(founder.path("Naziv").asString(""))));
        }
        return new JptEntry(
                node.path("Oznaka").asString(""), node.path("VrstaJPT").asString(""), node.path("OIB").asString(""),
                node.path("Naziv").asString("").strip(), parseDate(node.path("DatumOsnivanja").asString("")),
                collapseSpaces(node.path("Zupanija").asString("")), collapseSpaces(node.path("GradOpcina").asString("")),
                founders, jsonMapper.writeValueAsString(node));
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(text.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String collapseSpaces(String text) {
        return text == null || text.isBlank() ? null : text.strip().replaceAll("\\s+", " ");
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}
