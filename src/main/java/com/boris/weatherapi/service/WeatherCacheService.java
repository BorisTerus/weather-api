package com.boris.weatherapi.service;

import com.boris.weatherapi.dto.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WeatherCacheService {

    private final ExternalApiService externalApiService;

    @Cacheable(
            value = "weather-cache",
            key = "#city.toLowerCase().trim()",
            unless = "#result == null"
    )
    public WeatherResponseDto getWeather(String city) {
        var citySearch = externalApiService.fetchLatitudeAndLongitude(city);
        var cityWeather = citySearch.flatMap(c -> externalApiService
                .fetchWeather(c.lat(), c.lon()));

        return Objects.requireNonNull(cityWeather.block()).currentWeather();
    }
}
