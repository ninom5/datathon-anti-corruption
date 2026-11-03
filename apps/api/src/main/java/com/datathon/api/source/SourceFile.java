package com.datathon.api.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One downloaded resource. Kept even when the download or classification failed, so coverage can be reported. */
@Entity
@Table(name = "source_file")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id")
    private SourceDataset dataset;

    private String resourceId;
    private String name;
    private Instant resourceModified;
    private String url;
    private String format;

    @Enumerated(EnumType.STRING)
    private FormatClass formatClass;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(64)")
    private String sha256;

    private Long bytes;
    private Integer httpStatus;
    private String errorMessage;
    private Instant retrievedAt;
    private Long ingestRunId;

    public SourceFile(SourceDataset dataset, String url, Instant retrievedAt) {
        this.dataset = dataset;
        this.url = url;
        this.retrievedAt = retrievedAt;
    }
}
