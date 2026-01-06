package com.boris.weatherapi.service;

import com.boris.weatherapi.dto.WeatherResponseDto;
import com.boris.weatherapi.exception.TooManyRequestsException;
import com.boris.weatherapi.limiter.UserRateLimiter;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final UserRateLimiter rateLimiter;
    private final WeatherCacheService cacheService;

    public WeatherResponseDto get(String userId, String city) {
        RateLimiter limiter = rateLimiter.getLimiter(userId);

        CheckedSupplier<WeatherResponseDto> supplier =
                RateLimiter.decorateCheckedSupplier(
                        limiter,
                        () -> cacheService.getWeather(city)
                );

        try {
            return supplier.get();
        } catch (RequestNotPermitted ex) {
            throw new TooManyRequestsException(ex.getMessage());
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }
}
