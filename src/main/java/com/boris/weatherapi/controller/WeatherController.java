package com.boris.weatherapi.controller;

import com.boris.weatherapi.dto.WeatherResponseDto;
import com.boris.weatherapi.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherResponseDto getWeather(
            @RequestHeader("USER-ID") String userId,
            @RequestParam String city) {
        return weatherService.get(userId, city);
    }

}
