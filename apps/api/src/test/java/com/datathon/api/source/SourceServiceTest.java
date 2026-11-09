package com.datathon.api.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SourceServiceTest {

    @Test
    void showsEachFailedUrlOnceAndHidesFailuresOfDownloadedUrls() {
        SourceFile failedFirst = file(1L, "https://a.invalid/x.xlsx", null);
        SourceFile failedAgain = file(2L, "https://a.invalid/x.xlsx", null);
        SourceFile downloaded = file(3L, "https://b.invalid/y.xlsx", "f".repeat(64));
        SourceFile transientFailure = file(4L, "https://b.invalid/y.xlsx", null);

        assertThat(SourceService.withoutRepeatedFailures(List.of(failedFirst, failedAgain, downloaded, transientFailure)))
                .containsExactly(failedAgain, downloaded);
    }

    private static SourceFile file(Long id, String url, String sha256) {
        SourceFile file = new SourceFile(null, url, Instant.parse("2026-11-08T10:00:00Z"));
        file.setId(id);
        file.setSha256(sha256);
        return file;
    }
}
