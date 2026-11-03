package com.datathon.api.integration.anthropic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code javniradar.anthropic}. The key comes only from the ANTHROPIC_API_KEY environment variable. */
@ConfigurationProperties("javniradar.anthropic")
public record AnthropicProperties(String apiKey, String baseUrl, int connectTimeoutSeconds, int readTimeoutSeconds,
                                  int maxRetries) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Never print the key, e.g. when Spring logs a failed binding. */
    @Override
    public String toString() {
        return "AnthropicProperties[baseUrl=" + baseUrl + ", apiKey=" + (hasApiKey() ? "***" : "<none>") + "]";
    }
}
