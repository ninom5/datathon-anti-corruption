package com.datathon.api.common;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), common/NotFoundException.java.

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
