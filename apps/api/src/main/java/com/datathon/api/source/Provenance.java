package com.datathon.api.source;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** How a domain row was obtained: which raw record, which method and version, how confident. */
@Entity
@Getter
@Table(name = "provenance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Provenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_record_id")
    private RawRecord rawRecord;

    private ExtractionMethod extractionMethod;
    private String extractorVersion;
    private BigDecimal confidence;
    private Instant createdAt;

    public Provenance(RawRecord rawRecord, ExtractionMethod extractionMethod, String extractorVersion, BigDecimal confidence) {
        this.rawRecord = rawRecord;
        this.extractionMethod = extractionMethod;
        this.extractorVersion = extractorVersion;
        this.confidence = confidence;
        this.createdAt = Instant.now();
    }
}
