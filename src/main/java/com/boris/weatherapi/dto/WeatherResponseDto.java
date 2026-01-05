package com.boris.weatherapi.dto;

public record WeatherResponseDto(
        Double temperature,
        Double windspeed,
        String winddirection
) {
}
