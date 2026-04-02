package com.zorvyn.fintech.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Value("${app.rate-limit.requests}")
    private int requests;

    @Value("${app.rate-limit.duration-minutes}")
    private int durationMinutes;

    @Value("${app.rate-limit.auth-requests}")
    private int authRequests;

    @Value("${app.rate-limit.auth-duration-minutes}")
    private int authDurationMinutes;

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Get or create a rate limit bucket for a given IP.
     */
    public Bucket resolveBucket(String ip) {
        return bucketCache.computeIfAbsent(ip, this::newStandardBucket);
    }

    /**
     * Get or create a stricter rate limit bucket for auth endpoints.
     */
    public Bucket resolveAuthBucket(String ip) {
        return bucketCache.computeIfAbsent("auth:" + ip, k -> newAuthBucket());
    }

    private Bucket newStandardBucket(String key) {
        Bandwidth limit = Bandwidth.classic(requests, Refill.intervally(requests, Duration.ofMinutes(durationMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newAuthBucket() {
        Bandwidth limit = Bandwidth.classic(authRequests, Refill.intervally(authRequests, Duration.ofMinutes(authDurationMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }
}
