package com.datathon.api.integration.anthropic;

/**
 * Implementations: {@link AnthropicLlmClient} (real API), {@link NoopLlmClient} (no key) and, in tests,
 * {@code FixtureLlmClient} with prepared answers.
 */
public interface LlmClient {

    /** False without ANTHROPIC_API_KEY: the AI step is skipped and the UI shows "AI procjena nije dostupna". */
    boolean isConfigured();

    /**
     * @throws LlmUnavailableException the API cannot be used (no key, network, 429/5xx after the retry)
     * @throws LlmResponseException    the model answered, but not with a usable JSON object
     */
    LlmResult complete(LlmRequest request);
}
