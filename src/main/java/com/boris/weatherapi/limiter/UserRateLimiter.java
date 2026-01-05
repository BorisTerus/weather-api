package com.boris.weatherapi.limiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRateLimiter {

    private final ConcurrentHashMap<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();

    private RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ZERO)
            .build();

    public RateLimiter getLimiter(String userId) {
        return userLimiters.computeIfAbsent(userId,
                id -> RateLimiter.of("weather-" + id, config));
    }
}
