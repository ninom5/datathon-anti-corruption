package com.datathon.api.integration.anthropic;

public interface LlmClient {

    boolean isConfigured();

    LlmResult complete(LlmRequest request);
}
