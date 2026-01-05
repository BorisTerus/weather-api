package com.boris.weatherapi.service;

import com.boris.weatherapi.dto.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
        var cityWeather = externalApiService.fetchWeather(citySearch.lat(), citySearch.lon());
        return cityWeather.currentWeather();
    }
}
