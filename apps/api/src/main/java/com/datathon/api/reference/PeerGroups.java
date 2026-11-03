package com.datathon.api.reference;

/** Peer groups P1-P7 from docs/ANOMALY_DETECTION.md §3A (population from DZS Popis 2021). */
public final class PeerGroups {

    private PeerGroups() {
    }

    public static String of(String kind, int population) {
        return switch (kind) {
            case "grad_zagreb" -> "P7";
            case "opcina" -> population < 2_000 ? "P1" : population < 5_000 ? "P2" : "P3";
            case "grad" -> population < 10_000 ? "P4" : population < 50_000 ? "P5" : "P6";
            default -> throw new IllegalArgumentException("Nepoznata vrsta JLS: " + kind);
        };
    }
}
