package com.datathon.api.integration.ckan;

import java.time.Instant;

public record CkanResource(String id, String name, String url, String format, Instant lastModified, Instant created) {

    public Instant modifiedOrCreated() {
        return lastModified != null ? lastModified : created;
    }
}
