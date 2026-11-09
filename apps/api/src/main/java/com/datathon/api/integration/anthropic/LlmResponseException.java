package com.datathon.api.integration.anthropic;

public class LlmResponseException extends RuntimeException {

    private final transient LlmResult billedUsage;

    public LlmResponseException(String message, LlmResult billedUsage) {
        super(message);
        this.billedUsage = billedUsage;
    }

    public LlmResponseException(String message, Throwable cause) {
        super(message, cause);
        this.billedUsage = null;
    }

    public LlmResult billedUsage() {
        return billedUsage;
    }
}
