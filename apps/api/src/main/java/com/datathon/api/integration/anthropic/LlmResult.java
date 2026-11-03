package com.datathon.api.integration.anthropic;

import tools.jackson.databind.JsonNode;

/** The model's JSON answer with the token usage the cost is computed from. */
public record LlmResult(JsonNode answer, String model, long inputTokens, long outputTokens) {
}
