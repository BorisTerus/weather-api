package com.boris.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CityWeatherDto(
        @JsonProperty("current_weather")
        WeatherResponseDto currentWeather) {
}
