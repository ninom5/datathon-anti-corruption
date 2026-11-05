package com.datathon.api.integration.anthropic;

/** Used when ANTHROPIC_API_KEY is not set. */
public class NoopLlmClient implements LlmClient {

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public LlmResult complete(LlmRequest request) {
        throw new LlmUnavailableException("AI nije konfiguriran (ANTHROPIC_API_KEY nije postavljen).");
    }
}
