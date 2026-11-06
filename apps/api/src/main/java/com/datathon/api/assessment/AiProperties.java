package com.datathon.api.assessment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code javniradar.ai}: models, budget and prices from application.yaml, selection heuristics from
 * config/thresholds.yaml (docs/ANOMALY_DETECTION.md §4.1).
 */
@ConfigurationProperties("javniradar.ai")
public record AiProperties(String model, String recheckModel, String effort, int maxTokens, BigDecimal maxUsdPerRun,
                           int maxCallsPerRun, int concurrency, Map<String, Price> prices, Selection selection) {

    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    /** USD per million tokens. */
    public record Price(BigDecimal input, BigDecimal output) {
    }

    public record Selection(BigDecimal minValueEur, int minSubjectWords, int maxContractsPerRun, String source) {
    }

    /** A model without a configured price counts as free, which would break the budget, so it is refused. */
    public BigDecimal costUsd(String model, long inputTokens, long outputTokens) {
        Price price = prices.get(model);
        if (price == null) {
            throw new IllegalStateException("No price configured for model " + model + " (javniradar.ai.prices)");
        }
        return price.input().multiply(BigDecimal.valueOf(inputTokens))
                .add(price.output().multiply(BigDecimal.valueOf(outputTokens)))
                .divide(MILLION, 6, RoundingMode.HALF_UP);
    }
}
