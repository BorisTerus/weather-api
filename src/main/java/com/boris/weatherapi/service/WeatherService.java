package com.boris.weatherapi.service;

import com.boris.weatherapi.dto.WeatherResponseDto;
import com.boris.weatherapi.limiter.UserRateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final UserRateLimiter rateLimiter;
    private final WeatherCacheService cacheService;

    public Mono<WeatherResponseDto> get(String userId, String city) {
        RateLimiter limiter = rateLimiter.getLimiter(userId);

        return Mono.fromSupplier(() ->
                        cacheService.getWeather(city)
                )
                .transformDeferred(RateLimiterOperator.of(limiter));
    }


}
