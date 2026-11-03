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

/**
 * One model call for one context: prompt, structured output, usage and cost in {@code llm_usage}, and the validator.
 * Used by the AI run (contracts) and by the evaluation (fixed synthetic contexts).
 */
@Component
@RequiredArgsConstructor
public class AiModelCall {

    private final LlmClient llmClient;
    private final AiProperties properties;
    private final AiAssessmentWriter writer;
    private final JsonMapper jsonMapper;

    /** @param costUsd billed cost of the call, also when the answer turned out unusable */
    public record Outcome(AiValidation validation, BigDecimal costUsd) {
    }

    /**
     * @throws com.datathon.api.integration.anthropic.LlmUnavailableException the API cannot be reached
     * @throws CallFailedException the model answered but not usably; carries the billed cost
     */
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

    /** The model answered, but the answer cannot be used; the call was still billed. */
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
