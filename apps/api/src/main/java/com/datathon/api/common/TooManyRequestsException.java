package com.datathon.api.common;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), common/TooManyRequestsException.java.

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
