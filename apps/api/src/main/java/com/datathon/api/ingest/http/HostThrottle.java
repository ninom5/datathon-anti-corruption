package com.datathon.api.ingest.http;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class HostThrottle {

    interface Request<T> {
        T send() throws IOException, InterruptedException;
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final long minIntervalNanos;
    private long lastRequestNanos;
    private boolean hasRequested;

    HostThrottle(Duration minInterval) {
        this.minIntervalNanos = minInterval.toNanos();
    }

    <T> T send(Request<T> request) throws IOException, InterruptedException {
        lock.lock();
        try {
            waitForSlot();
            try {
                return request.send();
            } finally {
                lastRequestNanos = System.nanoTime();
                hasRequested = true;
            }
        } finally {
            lock.unlock();
        }
    }

    private void waitForSlot() throws InterruptedException {
        if (!hasRequested) {
            return;
        }
        long waitNanos = lastRequestNanos + minIntervalNanos - System.nanoTime();
        if (waitNanos > 0) {
            TimeUnit.NANOSECONDS.sleep(waitNanos);
        }
    }
}
