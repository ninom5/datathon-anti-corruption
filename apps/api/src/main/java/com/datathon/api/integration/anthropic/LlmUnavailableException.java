package com.datathon.api.integration.anthropic;

/** The model cannot be used right now (no key, API down, rate limit after the retry). */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message) {
        super(message);
    }

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
