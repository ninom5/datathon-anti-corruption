package com.datathon.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.assessment.AiRunResult;
import com.datathon.api.assessment.AiRunService;
import com.datathon.api.integration.anthropic.LlmClient;
import com.datathon.api.integration.anthropic.NoopLlmClient;
import com.datathon.api.rules.RulesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Starts the app against a throwaway PostgreSQL 17: proves the migrations apply and every entity matches them. The
 * key is forced empty, so this also proves the app and the rules run without ANTHROPIC_API_KEY (AGENTS.md).
 */
@SpringBootTest(properties = "javniradar.anthropic.api-key=")
@Import(TestcontainersConfiguration.class)
class ApiApplicationTests {

    @Autowired
    private LlmClient llmClient;
    @Autowired
    private AiRunService aiRunService;
    @Autowired
    private RulesService rulesService;

    @Test
    void startsAndRunsRulesWithoutAnthropicKey() {
        assertThat(llmClient).isInstanceOf(NoopLlmClient.class);
        assertThat(rulesService.runRules()).isNotNull();

        AiRunResult run = aiRunService.run(false, 5, false);
        assertThat(run.configured()).isFalse();
        assertThat(run.message()).isEqualTo("AI nije konfiguriran (ANTHROPIC_API_KEY nije postavljen).");
        assertThat(run.called()).isZero();

        assertThat(aiRunService.run(false, 5, true).dryRun()).isTrue();
    }
}
