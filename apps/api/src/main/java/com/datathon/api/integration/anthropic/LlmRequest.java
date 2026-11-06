package com.datathon.api.integration.anthropic;

import java.util.Map;

/**
 * One structured-output call. {@code jsonSchema} is a plain JSON Schema object (maps and lists), so no SDK or Jackson
 * type leaves the integration package.
 */
public record LlmRequest(String purpose, String model, String effort, int maxTokens, String systemPrompt,
                         String userText, Map<String, Object> jsonSchema) {
}
