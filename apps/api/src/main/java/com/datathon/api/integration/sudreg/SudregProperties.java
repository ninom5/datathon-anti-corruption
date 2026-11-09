package com.datathon.api.integration.sudreg;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("javniradar.sudreg")
public record SudregProperties(String baseUrl, String clientId, String clientSecret, long minIntervalMillis) {

    public boolean hasCredentials() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }

    @Override
    public String toString() {
        return "SudregProperties[baseUrl=" + baseUrl + ", credentials=" + (hasCredentials() ? "set" : "missing") + "]";
    }
}
