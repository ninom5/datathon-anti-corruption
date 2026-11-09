package com.datathon.api.assessment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

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
