package io.github.creezzi.tokenbucket;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketTest {
    private final AtomicLong now = new AtomicLong();

    private TokenBucket bucket(double rate, double capacity) {
        return new TokenBucket(rate, capacity, now::get);
    }

    @Test
    void allowsBurstThenBlocks() {
        TokenBucket b = bucket(1, 3);
        assertTrue(b.tryAcquire());
        assertTrue(b.tryAcquire());
        assertTrue(b.tryAcquire());
        assertFalse(b.tryAcquire());
    }

    @Test
    void refillsOverTimeUpToCapacity() {
        TokenBucket b = bucket(2, 4);
        assertTrue(b.tryAcquire(4));
        now.addAndGet(1_000_000_000L);
        assertTrue(b.tryAcquire(2));
        assertFalse(b.tryAcquire());
        now.addAndGet(100_000_000_000L);
        assertTrue(b.tryAcquire(4));
        assertFalse(b.tryAcquire());
    }

    @Test
    void reportsWaitTime() {
        TokenBucket b = bucket(2, 2);
        b.tryAcquire(2);
        assertEquals(500_000_000L, b.nanosUntilAvailable(1));
        now.addAndGet(500_000_000L);
        assertEquals(0, b.nanosUntilAvailable(1));
    }

    @Test
    void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> bucket(0, 1));
        assertThrows(IllegalArgumentException.class, () -> bucket(1, -1));
    }
}
