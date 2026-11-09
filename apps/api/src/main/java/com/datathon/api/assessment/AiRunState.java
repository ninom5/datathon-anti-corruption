package com.datathon.api.assessment;

import java.math.BigDecimal;

final class AiRunState {

    private final BigDecimal maxUsd;
    private final int maxCalls;
    private final int maxConsecutiveErrors;

    private BigDecimal costUsd = BigDecimal.ZERO;
    private int called;
    private int cacheHits;
    private int accepted;
    private int rejected;
    private int errors;
    private int consecutiveErrors;
    private int skipped;
    private String stopReason;

    AiRunState(BigDecimal maxUsd, int maxCalls, int maxConsecutiveErrors) {
        this.maxUsd = maxUsd;
        this.maxCalls = maxCalls;
        this.maxConsecutiveErrors = maxConsecutiveErrors;
    }

    synchronized boolean tryStartCall() {
        if (stopReason != null) {
            return false;
        }
        if (costUsd.compareTo(maxUsd) >= 0) {
            stopReason = "potrošen budžet od " + maxUsd + " USD";
            return false;
        }
        if (called >= maxCalls) {
            stopReason = "dosegnut najveći broj poziva (" + maxCalls + ")";
            return false;
        }
        called++;
        return true;
    }

    synchronized boolean isStopped() {
        return stopReason != null;
    }

    synchronized void addCost(BigDecimal cost) {
        costUsd = costUsd.add(cost);
    }

    synchronized void cacheHit() {
        cacheHits++;
    }

    synchronized void assessed(boolean isAccepted) {
        consecutiveErrors = 0;
        if (isAccepted) {
            accepted++;
        } else {
            rejected++;
        }
    }

    synchronized void error() {
        errors++;
        consecutiveErrors++;
        if (consecutiveErrors >= maxConsecutiveErrors && stopReason == null) {
            stopReason = maxConsecutiveErrors + " neuspjelih poziva zaredom (API nedostupan?)";
        }
    }

    synchronized void skip() {
        skipped++;
    }

    synchronized BigDecimal costUsd() {
        return costUsd;
    }

    synchronized int called() {
        return called;
    }

    synchronized int cacheHits() {
        return cacheHits;
    }

    synchronized int accepted() {
        return accepted;
    }

    synchronized int rejected() {
        return rejected;
    }

    synchronized int errors() {
        return errors;
    }

    synchronized int skipped() {
        return skipped;
    }

    synchronized String stopReason() {
        return stopReason;
    }
}
