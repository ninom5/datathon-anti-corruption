package com.datathon.api.ingest.http;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), ingest/http/HttpFetchException.java.

public class HttpFetchException extends RuntimeException {

    public HttpFetchException(String message) {
        super(message);
    }

    public HttpFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
