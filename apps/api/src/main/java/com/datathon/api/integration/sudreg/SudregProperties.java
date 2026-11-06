package com.datathon.api.integration.sudreg;

// Origin: copied from repo Desktop/datathon, integration/sudreg/SudregProperties.java; prefix javniradar.sudreg.

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credentials only from SUDREG_CLIENT_ID / SUDREG_CLIENT_SECRET; never logged. */
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
