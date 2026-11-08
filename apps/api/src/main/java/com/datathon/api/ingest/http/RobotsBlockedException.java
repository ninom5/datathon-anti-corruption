package com.datathon.api.ingest.http;

public class RobotsBlockedException extends RuntimeException {

    public RobotsBlockedException(String url) {
        super("Blocked by robots.txt: " + url);
    }
}
