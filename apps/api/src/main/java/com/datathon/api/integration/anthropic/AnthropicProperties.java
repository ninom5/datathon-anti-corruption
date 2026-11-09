package com.datathon.api.integration.anthropic;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("javniradar.anthropic")
public record AnthropicProperties(String apiKey, String baseUrl, int connectTimeoutSeconds, int readTimeoutSeconds,
                                  int maxRetries) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String toString() {
        return "AnthropicProperties[baseUrl=" + baseUrl + ", apiKey=" + (hasApiKey() ? "***" : "<none>") + "]";
    }
}
