package com.datathon.api.integration.anthropic;

import tools.jackson.databind.JsonNode;

public record LlmResult(JsonNode answer, String model, long inputTokens, long outputTokens) {
}
