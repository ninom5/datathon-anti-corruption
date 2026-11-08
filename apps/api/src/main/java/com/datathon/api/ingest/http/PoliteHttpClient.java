package com.datathon.api.ingest.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public class PoliteHttpClient {

    public static final String USER_AGENT = "JavniRadar/0.1 (studentski projekt; kontakt)";

    private static final Duration PAGE_MAX_AGE = Duration.ofHours(24);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final Set<Integer> RETRY_STATUSES = Set.of(429, 502, 503, 504);
    private static final int MAX_ATTEMPTS = 3;

    private final HttpClient httpClient;
    private final DiskCache cache;
    private final JsonMapper jsonMapper;
    private final Duration minInterval;
    private final Duration retryBackoff;
    private final Map<String, HostThrottle> throttles = new ConcurrentHashMap<>();
    private final Map<String, RobotsRules> robotsByHost = new ConcurrentHashMap<>();

    public PoliteHttpClient(DiskCache cache, JsonMapper jsonMapper, Duration minInterval, Duration retryBackoff) {
        this.cache = cache;
        this.jsonMapper = jsonMapper;
        this.minInterval = minInterval;
        this.retryBackoff = retryBackoff;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String getText(String url) {
        return getText(url, false);
    }

    public String getText(String url, boolean refresh) {
        return TextDecoder.decode(fetch(url, "html", PAGE_MAX_AGE, refresh));
    }

    public JsonNode getJson(String url) {
        return jsonMapper.readTree(TextDecoder.decode(fetch(url, "json", PAGE_MAX_AGE, false)));
    }

    public byte[] getBytes(String url) {
        return fetch(url, "bin", null, false);
    }

    /** Body plus the time it was really downloaded: provenance must not claim "now" for a cached copy. */
    public record Fetched(byte[] body, Instant retrievedAt) {
    }

    public Fetched getBinary(String url) {
        byte[] body = fetch(url, "bin", null, false);
        Instant retrievedAt = cache.lastModified(toUri(url), "bin").orElseGet(Instant::now);
        return new Fetched(body, retrievedAt);
    }

    private byte[] fetch(String url, String extension, Duration maxAge, boolean refresh) {
        URI uri = toUri(url);
        if (!refresh || maxAge == null) {
            Optional<byte[]> cached = cache.read(uri, extension, maxAge);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        if (!isAllowed(uri)) {
            log.warn("Skipping {} (robots.txt)", url);
            throw new RobotsBlockedException(url);
        }
        byte[] body = download(uri);
        cache.write(uri, extension, body);
        return body;
    }

    private boolean isAllowed(URI uri) {
        RobotsRules rules = robotsByHost.computeIfAbsent(uri.getScheme() + "://" + uri.getAuthority(), this::loadRobots);
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        return rules.isAllowed(Optional.ofNullable(uri.getRawPath()).orElse("/") + query);
    }

    private RobotsRules loadRobots(String origin) {
        try {
            HttpResponse<byte[]> response = send(URI.create(origin + "/robots.txt"));
            if (response.statusCode() != 200) {
                return RobotsRules.allowAll();
            }
            return RobotsRules.parse(TextDecoder.decode(response.body()), USER_AGENT);
        } catch (IOException e) {
            log.warn("Cannot read robots.txt of {}: {}", origin, e.getMessage());
            return RobotsRules.allowAll();
        }
    }

    private byte[] download(URI uri) {
        String lastProblem = "no attempt";
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<byte[]> response = send(uri);
                int status = response.statusCode();
                if (!RETRY_STATUSES.contains(status)) {
                    return requireSuccess(uri, response);
                }
                lastProblem = "HTTP " + status;
            } catch (IOException e) {
                lastProblem = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            pauseBeforeRetry(attempt);
        }
        throw new HttpFetchException("Failed after " + MAX_ATTEMPTS + " attempts (" + lastProblem + "): " + uri);
    }

    private byte[] requireSuccess(URI uri, HttpResponse<byte[]> response) {
        if (response.statusCode() >= 400) {
            throw new HttpFetchException("HTTP " + response.statusCode() + ": " + uri);
        }
        return response.body();
    }

    private HttpResponse<byte[]> send(URI uri) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(READ_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "hr,en;q=0.5")
                .GET()
                .build();
        HostThrottle throttle = throttles.computeIfAbsent(uri.getAuthority(), host -> new HostThrottle(minInterval));
        try {
            return throttle.send(() -> httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpFetchException("Interrupted: " + uri, e);
        }
    }

    private void pauseBeforeRetry(int attempt) {
        if (attempt == MAX_ATTEMPTS) {
            return;
        }
        try {
            Thread.sleep(retryBackoff.multipliedBy(attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpFetchException("Interrupted while waiting to retry", e);
        }
    }

    private static URI toUri(String url) {
        String withoutFragment = url.strip().split("#", 2)[0];
        URI uri = URI.create(withoutFragment.replace(" ", "%20"));
        return URI.create(uri.toASCIIString());
    }
}
