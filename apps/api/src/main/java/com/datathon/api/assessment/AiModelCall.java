package com.datathon.api.assessment;

import com.datathon.api.integration.anthropic.LlmClient;
import com.datathon.api.integration.anthropic.LlmRequest;
import com.datathon.api.integration.anthropic.LlmResponseException;
import com.datathon.api.integration.anthropic.LlmResult;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class AiModelCall {

    private final LlmClient llmClient;
    private final AiProperties properties;
    private final AiAssessmentWriter writer;
    private final JsonMapper jsonMapper;

    public record Outcome(AiValidation validation, BigDecimal costUsd) {
    }

    public Outcome assess(AiContext context, String model, String purpose) {
        LlmResult result;
        try {
            result = llmClient.complete(new LlmRequest(purpose, model, properties.effort(), properties.maxTokens(),
                    AiPrompt.systemPrompt(), AiPrompt.userText(context.canonicalJson()), AiPrompt.outputSchema()));
        } catch (LlmResponseException e) {
            throw new CallFailedException(e.getMessage(), recordCost(e.billedUsage(), purpose), e);
        }
        BigDecimal cost = recordCost(result, purpose);
        try {
            AiAnswer answer = jsonMapper.treeToValue(result.answer(), AiAnswer.class);
            return new Outcome(AiAssessmentValidator.validate(answer, context), cost);
        } catch (JacksonException e) {
            throw new CallFailedException("Model answer does not match the schema", cost, e);
        }
    }

    private BigDecimal recordCost(LlmResult usage, String purpose) {
        if (usage == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cost = properties.costUsd(usage.model(), usage.inputTokens(), usage.outputTokens());
        writer.recordUsage(purpose, usage.model(), usage.inputTokens(), usage.outputTokens(), cost, false);
        return cost;
    }

    public static class CallFailedException extends RuntimeException {

        private final transient BigDecimal costUsd;

        CallFailedException(String message, BigDecimal costUsd, Throwable cause) {
            super(message, cause);
            this.costUsd = costUsd;
        }

        public BigDecimal costUsd() {
            return costUsd;
        }
    }
}
