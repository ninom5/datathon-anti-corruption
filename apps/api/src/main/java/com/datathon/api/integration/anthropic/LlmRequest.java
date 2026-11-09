package com.datathon.api.integration.anthropic;

import java.util.Map;

public record LlmRequest(String purpose, String model, String effort, int maxTokens, String systemPrompt,
                         String userText, Map<String, Object> jsonSchema) {
}
