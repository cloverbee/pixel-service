package com.multiverse.pixel.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_SECOND = 100;
    private final StringRedisTemplate redisTemplate;

    /**
     * Lua script executes ATOMICALLY on Redis server.
     * No other command can run between these operations!
     * 
     * This solves the race condition because:
     * - All operations happen as ONE atomic unit
     * - No thread can "slip through" between check and add
     */
    private static final String RATE_LIMIT_SCRIPT =
            "local key = KEYS[1]\n" +
            "local now = tonumber(ARGV[1])\n" +
            "local window_start = now - 1000\n" +
            "local max_requests = tonumber(ARGV[2])\n" +
            "local unique_id = ARGV[3]\n" +
            "\n" +
            "-- Step 1: Remove old entries (atomic)\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, window_start)\n" +
            "\n" +
            "-- Step 2: Count current entries (atomic)\n" +
            "local count = redis.call('ZCARD', key)\n" +
            "\n" +
            "-- Step 3: Check limit (atomic)\n" +
            "if count >= max_requests then\n" +
            "    return -1  -- Rate limited\n" +
            "end\n" +
            "\n" +
            "-- Step 4: Add this request (atomic)\n" +
            "redis.call('ZADD', key, now, unique_id)\n" +
            "redis.call('EXPIRE', key, 2)\n" +
            "\n" +
            "return max_requests - count - 1  -- Return remaining\n";

    private final DefaultRedisScript<Long> rateLimitScript;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);
    }

    /**
     * Check if the user has exceeded rate limit.
     * Uses Redis Lua script for ATOMIC execution - no race conditions!
     *
     * @param userId The user/team ID
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(String userId) {
        String key = "rate_limit:" + userId;
        long now = System.currentTimeMillis();
        // Unique ID for this request (prevents duplicate entries in sorted set)
        String uniqueId = now + ":" + Thread.currentThread().getId() + ":" + Math.random();

        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(MAX_REQUESTS_PER_SECOND),
                uniqueId
        );

        // -1 = rate limited, >= 0 = allowed (number is remaining requests)
        return result != null && result >= 0;
    }

    public int getRemaining(String userId) {
        String key = "rate_limit:" + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - 1000;

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(key);

        return MAX_REQUESTS_PER_SECOND - (count != null ? count.intValue() : 0);
    }
}