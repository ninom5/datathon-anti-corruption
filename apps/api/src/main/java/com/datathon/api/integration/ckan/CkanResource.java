package com.datathon.api.integration.ckan;

import java.time.Instant;

public record CkanResource(String id, String name, String url, String format, Instant lastModified, Instant created) {

    /** Used to pick the newest file when the same contract appears in several files. */
    public Instant modifiedOrCreated() {
        return lastModified != null ? lastModified : created;
    }
}
