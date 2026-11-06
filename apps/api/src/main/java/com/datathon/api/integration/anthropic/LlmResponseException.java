package com.datathon.api.integration.anthropic;

/** The model answered, but the answer is cut off, refused or not a JSON object. Usage may still have been billed. */
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

    /** Tokens of a call that was billed although its answer is unusable; null when nothing is known. */
    public LlmResult billedUsage() {
        return billedUsage;
    }
}
