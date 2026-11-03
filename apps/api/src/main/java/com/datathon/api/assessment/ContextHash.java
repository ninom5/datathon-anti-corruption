package com.datathon.api.assessment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Cache key of an assessment: {@code sha256(prompt_version + model + canonical context)} (§4.1). */
final class ContextHash {

    private ContextHash() {
    }

    static String of(String promptVersion, String model, String canonicalJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    (promptVersion + "\n" + model + "\n" + canonicalJson).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
