# tokenbucket

A token bucket rate limiter with an injectable nanosecond clock.

## Usage

```java
TokenBucket bucket = new TokenBucket(10, 20); // 10 permits/sec, burst of 20
if (bucket.tryAcquire()) {
    handle(request);
}
```

## Test

```bash
mvn test
```

## License

MIT
