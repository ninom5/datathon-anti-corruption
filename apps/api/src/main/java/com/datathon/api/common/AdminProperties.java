package com.datathon.api.common;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), common/AdminProperties.java.

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("admin")
@Getter
@RequiredArgsConstructor
public class AdminProperties {

    private final String key;
}
