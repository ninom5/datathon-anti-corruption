package com.datathon.api.source;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataCoverage {

    private static final ZoneId CROATIA = ZoneId.of("Europe/Zagreb");

    private final SourceFileRepository sourceFileRepository;

    public int incompleteFromYear() {
        Instant latest = sourceFileRepository.findLatestRealRetrieval();
        return latest == null ? Year.now(CROATIA).getValue() : Year.from(latest.atZone(CROATIA)).getValue();
    }
}
