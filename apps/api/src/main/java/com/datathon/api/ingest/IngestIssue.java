package com.datathon.api.ingest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "ingest_issue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestIssue {

    public static final String ERROR = "error";
    public static final String WARNING = "warning";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long runId;
    private Long fileId;
    private Long rawRecordId;
    private String severity;
    private String code;
    private String message;

    public IngestIssue(Long runId, Long fileId, Long rawRecordId, String severity, String code, String message) {
        this.runId = runId;
        this.fileId = fileId;
        this.rawRecordId = rawRecordId;
        this.severity = severity;
        this.code = code;
        this.message = message;
    }
}
