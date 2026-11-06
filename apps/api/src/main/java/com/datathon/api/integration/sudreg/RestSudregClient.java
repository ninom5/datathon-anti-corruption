package com.datathon.api.integration.sudreg;

// Origin: copied from repo Desktop/datathon, integration/sudreg/RestSudregClient.java; changed: answer parsed into
// SudregCompany (NKD from pretezita_djelatnost, founding date, procedures), 404/empty answer = Optional.empty().

import com.datathon.api.common.ExternalSourceException;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * OAuth2 client credentials; the token (6 h in the 2026-11-04 check) is kept in memory. Exception messages never
 * carry the request URL, the OIB or the credentials, because exception text is logged.
 */
public class RestSudregClient implements SudregClient {

    private static final String SOURCE = "Sudreg";
    private static final String TOKEN_PATH = "/api/oauth/token";
    private static final String DETAILS_PATH = "/api/javni/detalji_subjekta";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration TOKEN_EXPIRY_MARGIN = Duration.ofSeconds(30);
    private static final long DEFAULT_TOKEN_SECONDS = 300;

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String clientId;
    private final String clientSecret;
    private final Clock clock;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.MIN;

    public RestSudregClient(SudregProperties properties, JsonMapper jsonMapper, Clock clock) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, "JavniRadar/0.1 (studentski projekt)").build();
        this.jsonMapper = jsonMapper;
        this.clientId = properties.clientId();
        this.clientSecret = properties.clientSecret();
        this.clock = clock;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Optional<SudregCompany> findByOib(String oib) {
        Optional<String> body = fetchDetails(oib);
        if (body.isEmpty() || body.get().isBlank()) {
            return Optional.empty();
        }
        JsonNode answer = parse(body.get());
        if (answer.path("oib").isMissingNode() || answer.path("oib").isNull()) {
            return Optional.empty();
        }
        return Optional.of(SudregCompany.from(answer));
    }

    private Optional<String> fetchDetails(String oib) {
        try {
            return Optional.ofNullable(requestDetails(oib, token()));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            if (e.getStatusCode().value() != 401) {
                throw failure(e.getStatusCode());
            }
            forgetToken();
        } catch (RestClientException e) {
            throw new ExternalSourceException(SOURCE, "the register is unreachable");
        }
        return retryWithNewToken(oib);
    }

    /** A token can be revoked before it expires, so one 401 gets a single retry with a fresh token. */
    private Optional<String> retryWithNewToken(String oib) {
        try {
            return Optional.ofNullable(requestDetails(oib, token()));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw failure(e.getStatusCode());
        } catch (RestClientException e) {
            throw new ExternalSourceException(SOURCE, "the register is unreachable");
        }
    }

    private String requestDetails(String oib, String token) {
        return restClient.get()
                .uri(uri -> uri.path(DETAILS_PATH)
                        .queryParam("tip_identifikatora", "oib")
                        .queryParam("identifikator", oib)
                        .queryParam("expand_relations", "true")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(String.class);
    }

    private static ExternalSourceException failure(HttpStatusCode status) {
        return new ExternalSourceException(SOURCE, "the register answered HTTP " + status.value());
    }

    private JsonNode parse(String body) {
        try {
            return jsonMapper.readTree(body);
        } catch (JacksonException e) {
            throw new ExternalSourceException(SOURCE, "the register answer cannot be read");
        }
    }

    /** Keeps a safety margin, but never more than half of a short lifetime, so a 30 s token is still reused. */
    private static Duration usableLifetime(Duration lifetime) {
        Duration margin = TOKEN_EXPIRY_MARGIN.compareTo(lifetime.dividedBy(2)) < 0
                ? TOKEN_EXPIRY_MARGIN
                : lifetime.dividedBy(2);
        return lifetime.minus(margin);
    }

    private synchronized String token() {
        if (accessToken == null || !clock.instant().isBefore(accessTokenExpiresAt)) {
            requestToken();
        }
        return accessToken;
    }

    private synchronized void forgetToken() {
        accessToken = null;
    }

    private void requestToken() {
        JsonNode answer;
        try {
            String body = restClient.post()
                    .uri(TOKEN_PATH)
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(String.class);
            answer = jsonMapper.readTree(body);
        } catch (HttpStatusCodeException e) {
            throw new ExternalSourceException(SOURCE, "token request answered HTTP " + e.getStatusCode().value());
        } catch (RestClientException | JacksonException e) {
            throw new ExternalSourceException(SOURCE, "token request failed");
        }
        String token = answer.path("access_token").asString("");
        if (token.isBlank()) {
            throw new ExternalSourceException(SOURCE, "token answer has no access token");
        }
        JsonNode lifetime = answer.path("expires_in");
        long seconds = lifetime.isNumber() ? lifetime.longValue() : DEFAULT_TOKEN_SECONDS;
        accessToken = token;
        accessTokenExpiresAt = clock.instant().plus(usableLifetime(Duration.ofSeconds(seconds)));
    }
}
