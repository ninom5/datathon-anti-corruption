package com.datathon.api.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@EnableConfigurationProperties(AdminProperties.class)
@RequiredArgsConstructor
public class AdminKeyGuard {

    private final AdminProperties properties;

    public void verify(String providedKey) {
        String expected = properties.getKey();
        boolean isValid = expected != null && !expected.isBlank() && providedKey != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                        providedKey.getBytes(StandardCharsets.UTF_8));
        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing admin key");
        }
    }
}
