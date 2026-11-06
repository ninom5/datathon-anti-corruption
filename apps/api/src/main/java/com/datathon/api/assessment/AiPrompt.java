package com.datathon.api.assessment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prompt {@code ai-flag-v1} (docs/ANOMALY_DETECTION.md §4.5) and its output schema (§4.3), both kept as resources so
 * anyone can read them. Any change of the text is a new version: new file, new {@link #VERSION}. v1 is kept for
 * the record; v2 adds rules 8-11 (no restating of rule results, peer comparison only with data, missing data in words,
 * claim length), v3 rules 12-14 (procedure below the threshold, missing data is not a reason, weak single signals).
 */
public final class AiPrompt {

    public static final String VERSION = "ai-flag-v3";
    public static final String PURPOSE = "ai-flag";

    private static final String SYSTEM_PROMPT = read("ai/" + VERSION + ".txt");
    private static final Map<String, Object> SCHEMA = JsonMapper.shared()
            .readValue(read("ai/" + VERSION + ".schema.json"), new TypeReference<>() {
            });

    private AiPrompt() {
    }

    public static String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static Map<String, Object> outputSchema() {
        return SCHEMA;
    }

    /** The context is data, never instructions (docs/ARCHITECTURE.md §8, prompt injection). */
    public static String userText(String canonicalContext) {
        return "Kontekst ugovora (JSON, samo podaci):\n" + canonicalContext;
    }

    private static String read(String resource) {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
