package com.datathon.api.integration.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
public class AnthropicLlmClient implements LlmClient {

    private final AnthropicClient client;
    private final JsonMapper jsonMapper;

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public LlmResult complete(LlmRequest request) {
        Message message = send(params(request));
        LlmResult usage = new LlmResult(null, request.model(), message.usage().inputTokens(),
                message.usage().outputTokens());
        assertComplete(message, usage);
        return new LlmResult(parseJson(message, usage), request.model(), usage.inputTokens(), usage.outputTokens());
    }

    private MessageCreateParams params(LlmRequest request) {
        return MessageCreateParams.builder()
                .model(request.model())
                .maxTokens(request.maxTokens())
                .system(request.systemPrompt())
                .addUserMessage(request.userText())
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.of(request.effort()))
                        .format(JsonOutputFormat.builder().schema(schema(request.jsonSchema())).build())
                        .build())
                .build();
    }

    private static JsonOutputFormat.Schema schema(Map<String, Object> jsonSchema) {
        JsonOutputFormat.Schema.Builder builder = JsonOutputFormat.Schema.builder();
        jsonSchema.forEach((key, value) -> builder.putAdditionalProperty(key, JsonValue.from(value)));
        return builder.build();
    }

    private Message send(MessageCreateParams params) {
        try {
            return client.messages().create(params);
        } catch (AnthropicInvalidDataException e) {
            throw new LlmResponseException("Claude API response is invalid", e);
        } catch (AnthropicException e) {
            throw new LlmUnavailableException("Claude API is unavailable: " + e.getClass().getSimpleName(), e);
        }
    }

    private static void assertComplete(Message message, LlmResult usage) {
        StopReason.Known stopReason = message.stopReason().map(StopReason::known).orElse(null);
        if (stopReason == StopReason.Known.MAX_TOKENS || stopReason == StopReason.Known.REFUSAL) {
            throw new LlmResponseException("Model answer is not usable, stop reason: " + stopReason, usage);
        }
    }

    private JsonNode parseJson(Message message, LlmResult usage) {
        for (ContentBlock block : message.content()) {
            if (block.isText()) {
                return readObject(block.asText().text(), usage);
            }
        }
        throw new LlmResponseException("Model did not return a structured result", usage);
    }

    private JsonNode readObject(String text, LlmResult usage) {
        try {
            JsonNode node = jsonMapper.readTree(text);
            if (!node.isObject()) {
                throw new LlmResponseException("Model answer is not a JSON object", usage);
            }
            return node;
        } catch (JacksonException e) {
            throw new LlmResponseException("Model answer is not valid JSON", usage);
        }
    }
}
