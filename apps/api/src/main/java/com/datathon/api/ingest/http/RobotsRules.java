package com.datathon.api.ingest.http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RobotsRules {

    private static final RobotsRules ALLOW_ALL = new RobotsRules(List.of());

    private final List<String> disallowedPrefixes;

    private RobotsRules(List<String> disallowedPrefixes) {
        this.disallowedPrefixes = disallowedPrefixes;
    }

    public static RobotsRules allowAll() {
        return ALLOW_ALL;
    }

    public static RobotsRules parse(String content, String agentToken) {
        List<String> forAgent = null;
        List<String> forAll = null;
        List<String> groupRules = new ArrayList<>();
        boolean readingAgents = false;
        for (String rawLine : content.replace("\uFEFF", "").split("\\R")) {
            String line = stripComment(rawLine);
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String field = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();
            if (field.equals("user-agent")) {
                if (!readingAgents) {
                    groupRules = new ArrayList<>();
                    readingAgents = true;
                }
                if (matchesAgent(value, agentToken) && forAgent == null) {
                    forAgent = groupRules;
                }
                if (value.equals("*") && forAll == null) {
                    forAll = groupRules;
                }
                continue;
            }
            readingAgents = false;
            if (field.equals("disallow") && !value.isEmpty()) {
                groupRules.add(normalize(value));
            }
        }
        List<String> rules = forAgent != null ? forAgent : forAll;
        return rules == null || rules.isEmpty() ? ALLOW_ALL : new RobotsRules(List.copyOf(rules));
    }

    public boolean isAllowed(String pathAndQuery) {
        String path = normalize(pathAndQuery.isEmpty() ? "/" : pathAndQuery);
        return disallowedPrefixes.stream().noneMatch(path::startsWith);
    }

    private static boolean matchesAgent(String value, String agentToken) {
        return !value.equals("*") && agentToken.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT));
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash < 0 ? line : line.substring(0, hash);
    }

    private static String normalize(String path) {
        try {
            return URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return path;
        }
    }
}
