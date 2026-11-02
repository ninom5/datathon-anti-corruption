package com.datathon.api.ingest.eojn;

/** Result of reading one data row of a class A register. */
public sealed interface RowOutcome {

    record Parsed(ParsedContract contract) implements RowOutcome {
    }

    /** {@code code} is one of the row rejection codes in {@link com.datathon.api.ingest.IssueCode}. */
    record Rejected(String code, String message) implements RowOutcome {
    }

    /** Empty spacer row; not counted as a rejection. */
    record Blank() implements RowOutcome {
    }
}
