package com.datathon.api.ingest.http;

public class HttpFetchException extends RuntimeException {

    public HttpFetchException(String message) {
        super(message);
    }

    public HttpFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
