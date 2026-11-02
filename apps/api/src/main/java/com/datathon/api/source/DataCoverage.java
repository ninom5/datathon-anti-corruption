package com.datathon.api.source;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Which signing years are incomplete. A year that was not over when the registers were last retrieved has partial
 * totals (2026 in the POC), and the UI must say so instead of comparing it with full years.
 */
@Component
@RequiredArgsConstructor
public class DataCoverage {

    private static final ZoneId CROATIA = ZoneId.of("Europe/Zagreb");

    private final SourceFileRepository sourceFileRepository;

    /** Signing years from this one on are incomplete. */
    public int incompleteFromYear() {
        Instant latest = sourceFileRepository.findLatestRealRetrieval();
        return latest == null ? Year.now(CROATIA).getValue() : Year.from(latest.atZone(CROATIA)).getValue();
    }
}
