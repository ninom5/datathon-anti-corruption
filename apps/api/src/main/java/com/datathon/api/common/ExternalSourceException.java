package com.datathon.api.common;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), common/ExternalSourceException.java.

import lombok.Getter;

/** An external source (Narodne novine, Sudreg, ...) did not give a usable answer. */
@Getter
public class ExternalSourceException extends RuntimeException {

    private final String source;

    public ExternalSourceException(String source, String message, Throwable cause) {
        super(source + ": " + message, cause);
        this.source = source;
    }

    public ExternalSourceException(String source, String message) {
        this(source, message, null);
    }
}
