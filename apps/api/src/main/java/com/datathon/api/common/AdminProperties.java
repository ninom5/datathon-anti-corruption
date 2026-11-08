package com.datathon.api.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("admin")
@Getter
@RequiredArgsConstructor
public class AdminProperties {

    private final String key;
}
