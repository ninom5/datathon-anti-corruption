package com.datathon.api.rules;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RuleThresholds.class)
public class RulesConfig {
}
