package com.datathon.api.source;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "source_dataset")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceDataset {

    public static final String PORTAL_DATA_GOV_HR = "data.gov.hr";
    public static final String PORTAL_SYNTHETIC = "synthetic";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String portal;
    private String externalId;
    private String title;
    private String publisher;
    private String licenseId;
    private String landingUrl;
    private Instant metadataModified;
    private boolean isSynthetic;

    public SourceDataset(String portal, String externalId) {
        this.portal = portal;
        this.externalId = externalId;
    }
}
