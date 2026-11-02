package com.datathon.api.rules;

/** A link that lets anyone check the indicator against the source (docs/ANOMALY_DETECTION.md §0.3). */
public record EvidenceLink(String label, String url, String field) {
}
