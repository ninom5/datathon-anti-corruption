package com.datathon.api.assessment;

// Origin: copied from repo Desktop/datathon, ai/LlmUsage.java; changed: cache_hit column, cost scale of DATA_MODEL.md.

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One model call (or cache hit) with tokens and cost (docs/ARCHITECTURE.md §7). */
@Entity
@Table(name = "llm_usage")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String purpose;
    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal costUsd;
    private Boolean cacheHit;
    private Instant createdAt;

    public LlmUsage(String purpose, String model, int inputTokens, int outputTokens, BigDecimal costUsd,
                    boolean cacheHit) {
        this.purpose = purpose;
        this.model = model;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd = costUsd;
        this.cacheHit = cacheHit;
        this.createdAt = Instant.now();
    }
}
