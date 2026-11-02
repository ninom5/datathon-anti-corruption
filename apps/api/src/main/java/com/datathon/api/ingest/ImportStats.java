package com.datathon.api.ingest;

import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;

/** Counters of one import, summed over files into {@code ingest_run.stats}. */
@Getter
public class ImportStats {

    private int rows;
    private int created;
    private int superseded;
    private final Map<String, Integer> rejectedByCode = new TreeMap<>();
    private final Map<String, Integer> warningsByCode = new TreeMap<>();

    void countRow() {
        rows++;
    }

    void countCreated() {
        created++;
    }

    void countSuperseded() {
        superseded++;
    }

    void countRejected(String code) {
        rejectedByCode.merge(code, 1, Integer::sum);
    }

    void countWarning(String code) {
        warningsByCode.merge(code, 1, Integer::sum);
    }

    public int rejected() {
        return rejectedByCode.values().stream().mapToInt(Integer::intValue).sum();
    }

    void add(ImportStats other) {
        rows += other.rows;
        created += other.created;
        superseded += other.superseded;
        other.rejectedByCode.forEach((code, count) -> rejectedByCode.merge(code, count, Integer::sum));
        other.warningsByCode.forEach((code, count) -> warningsByCode.merge(code, count, Integer::sum));
    }
}
