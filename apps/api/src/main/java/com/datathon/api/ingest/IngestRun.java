package com.datathon.api.ingest;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ingest_run")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestRun {

    public static final String RUNNING = "running";
    public static final String FINISHED = "finished";
    public static final String FAILED = "failed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant startedAt;
    private Instant finishedAt;
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    private String stats;

    public static IngestRun start() {
        IngestRun run = new IngestRun();
        run.startedAt = Instant.now();
        run.status = RUNNING;
        return run;
    }
}
