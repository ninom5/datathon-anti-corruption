package com.datathon.api.ingest;

import java.time.Instant;

/** {@code stats} is the raw JSON stored in ingest_run.stats. */
public record IngestRunResponse(Long id, String status, Instant startedAt, Instant finishedAt, String stats) {

    public static IngestRunResponse from(IngestRun run) {
        return new IngestRunResponse(run.getId(), run.getStatus(), run.getStartedAt(), run.getFinishedAt(), run.getStats());
    }
}
