package com.datathon.api.integration.anthropic;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.Timeout;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/** Without ANTHROPIC_API_KEY the app starts normally with {@link NoopLlmClient}; the rules never need the key. */
@Slf4j
@Configuration
@EnableConfigurationProperties(AnthropicProperties.class)
public class LlmConfig {

    @Bean
    LlmClient llmClient(AnthropicProperties properties, JsonMapper jsonMapper) {
        if (!properties.hasApiKey()) {
            log.info("ANTHROPIC_API_KEY is not set: AI assessment is off, rules run as usual");
            return new NoopLlmClient();
        }
        Duration read = Duration.ofSeconds(properties.readTimeoutSeconds());
        return new AnthropicLlmClient(AnthropicOkHttpClient.builder()
                .apiKey(properties.apiKey())
                .baseUrl(properties.baseUrl())
                .maxRetries(properties.maxRetries())
                .timeout(Timeout.builder()
                        .connect(Duration.ofSeconds(properties.connectTimeoutSeconds()))
                        .read(read)
                        .request(read)
                        .build())
                .build(), jsonMapper);
    }
}
