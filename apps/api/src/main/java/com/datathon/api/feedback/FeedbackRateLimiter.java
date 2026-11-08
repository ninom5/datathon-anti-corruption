package com.datathon.api.feedback;

import com.datathon.api.common.TooManyRequestsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 10 submissions per minute per client address (docs/ARCHITECTURE.md §8), in memory. Enough for one instance and
 * the demo, so no Bucket4j dependency; addresses are kept only for the window and never logged.
 */
@Component
public class FeedbackRateLimiter {

    static final int LIMIT = 10;
    static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> recentByClient = new ConcurrentHashMap<>();
    private final Clock clock;

    public FeedbackRateLimiter() {
        this(Clock.systemUTC());
    }

    FeedbackRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void acquire(String clientAddress) {
        Instant now = clock.instant();
        Deque<Instant> recent = recentByClient.computeIfAbsent(clientAddress, key -> new ArrayDeque<>());
        synchronized (recent) {
            while (!recent.isEmpty() && recent.peekFirst().isBefore(now.minus(WINDOW))) {
                recent.pollFirst();
            }
            if (recent.size() >= LIMIT) {
                throw new TooManyRequestsException("feedback limit reached");
            }
            recent.addLast(now);
        }
    }
}
