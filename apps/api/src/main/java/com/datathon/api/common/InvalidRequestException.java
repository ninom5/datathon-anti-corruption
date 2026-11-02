package com.datathon.api.common;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), common/InvalidRequestException.java.

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
