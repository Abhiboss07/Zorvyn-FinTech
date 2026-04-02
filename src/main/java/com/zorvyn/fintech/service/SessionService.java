package com.zorvyn.fintech.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Redis-backed session management.
 * Key pattern: session:{userId}
 * TTL: configurable (default 30 minutes), extended on each authenticated request.
 */
@Service
public class SessionService {

    private static final String SESSION_PREFIX = "session:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration sessionTimeout;

    public SessionService(RedisTemplate<String, Object> redisTemplate,
                           @Value("${app.session.timeout-minutes:30}") int timeoutMinutes) {
        this.redisTemplate = redisTemplate;
        this.sessionTimeout = Duration.ofMinutes(timeoutMinutes);
    }

    /**
     * Create a new session for a user.
     */
    public void createSession(String userId, String email, String role, String ipAddress) {
        String key = SESSION_PREFIX + userId;
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("email", email);
        sessionData.put("role", role);
        sessionData.put("ipAddress", ipAddress);
        sessionData.put("loginTime", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll(key, sessionData);
        redisTemplate.expire(key, sessionTimeout);
    }

    /**
     * Check if a session exists and is valid.
     */
    public boolean isSessionValid(String userId) {
        String key = SESSION_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Extend session TTL on each authenticated request.
     */
    public void extendSession(String userId) {
        String key = SESSION_PREFIX + userId;
        redisTemplate.expire(key, sessionTimeout);
    }

    /**
     * Destroy session on logout.
     */
    public void destroySession(String userId) {
        String key = SESSION_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * Get session data.
     */
    public Map<Object, Object> getSession(String userId) {
        String key = SESSION_PREFIX + userId;
        return redisTemplate.opsForHash().entries(key);
    }
}
