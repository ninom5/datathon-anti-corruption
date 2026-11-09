package com.datathon.api.ingest.eojn;

public sealed interface RowOutcome {

    record Parsed(ParsedContract contract) implements RowOutcome {
    }

    record Rejected(String code, String message) implements RowOutcome {
    }

    record Blank() implements RowOutcome {
    }
}
