package com.datathon.api.integration.anthropic;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class FixtureLlmClient implements LlmClient {

    public static final String UNAVAILABLE = "nedostupno";
    public static final long INPUT_TOKENS = 1000;
    public static final long OUTPUT_TOKENS = 200;

    private final JsonMapper jsonMapper = JsonMapper.shared();
    private final Deque<String> queue = new ArrayDeque<>();
    private final AtomicInteger calls = new AtomicInteger();
    private String defaultFixture = "vrijedi-provjeriti";

    public synchronized void enqueue(String... fixtureNames) {
        queue.addAll(java.util.List.of(fixtureNames));
    }

    public synchronized void answerAlwaysWith(String fixtureName) {
        queue.clear();
        defaultFixture = fixtureName;
    }

    public int calls() {
        return calls.get();
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public LlmResult complete(LlmRequest request) {
        calls.incrementAndGet();
        String fixture = next();
        if (UNAVAILABLE.equals(fixture)) {
            throw new LlmUnavailableException("fixture: API unavailable");
        }
        String sourceUrl = sourceUrlOf(request.userText());
        String body = read("fixtures/ai/" + fixture + ".json").replace("{{ugovor.izvor_url}}", sourceUrl);
        return new LlmResult(jsonMapper.readTree(body), request.model(), INPUT_TOKENS, OUTPUT_TOKENS);
    }

    private synchronized String next() {
        return queue.isEmpty() ? defaultFixture : queue.poll();
    }

    private String sourceUrlOf(String userText) {
        JsonNode context = jsonMapper.readTree(userText.substring(userText.indexOf('{')));
        return context.path("ugovor").path("izvor_url").asString("");
    }

    private static String read(String resource) {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
