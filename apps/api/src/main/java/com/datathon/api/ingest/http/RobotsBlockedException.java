package com.datathon.api.ingest.http;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), ingest/http/RobotsBlockedException.java.

public class RobotsBlockedException extends RuntimeException {

    public RobotsBlockedException(String url) {
        super("Blocked by robots.txt: " + url);
    }
}
