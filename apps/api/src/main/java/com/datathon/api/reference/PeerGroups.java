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

    /** Text for the UI, e.g. "općine od 2.000 do 4.999 stanovnika". */
    public static String labelOf(String peerGroup) {
        return switch (peerGroup) {
            case "P1" -> "općine do 1.999 stanovnika";
            case "P2" -> "općine od 2.000 do 4.999 stanovnika";
            case "P3" -> "općine od 5.000 stanovnika";
            case "P4" -> "gradovi do 9.999 stanovnika";
            case "P5" -> "gradovi od 10.000 do 49.999 stanovnika";
            case "P6" -> "gradovi od 50.000 stanovnika (bez Zagreba)";
            case "P7" -> "Grad Zagreb (nema usporedive skupine)";
            default -> "nepoznata skupina";
        };
    }
}
