package io.github.creezzi.tokenbucket;

import java.util.function.LongSupplier;

/** Thread-safe token bucket. Rate is permits per second. */
public final class TokenBucket {
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final double ratePerSecond;
    private final double capacity;
    private final LongSupplier nanoClock;
    private double tokens;
    private long lastNanos;

    public TokenBucket(double ratePerSecond, double capacity) {
        this(ratePerSecond, capacity, System::nanoTime);
    }

    public TokenBucket(double ratePerSecond, double capacity, LongSupplier nanoClock) {
        if (ratePerSecond <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("rate and capacity must be positive");
        }
        this.ratePerSecond = ratePerSecond;
        this.capacity = capacity;
        this.nanoClock = nanoClock;
        this.tokens = capacity;
        this.lastNanos = nanoClock.getAsLong();
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        double elapsedSeconds = Math.max(0, now - lastNanos) / NANOS_PER_SECOND;
        tokens = Math.min(capacity, tokens + elapsedSeconds * ratePerSecond);
        lastNanos = now;
    }

    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public synchronized boolean tryAcquire(double permits) {
        refill();
        if (permits <= tokens) {
            tokens -= permits;
            return true;
        }
        return false;
    }

    /** Nanoseconds until the given number of permits would be available (0 if now). */
    public synchronized long nanosUntilAvailable(double permits) {
        refill();
        double missing = permits - tokens;
        return missing <= 0 ? 0 : (long) Math.ceil(missing / ratePerSecond * NANOS_PER_SECOND);
    }
}
