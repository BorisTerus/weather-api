package com.boris.weatherapi.service;

import com.boris.weatherapi.config.CityPropertiesConfig;
import com.boris.weatherapi.dto.CitySearchDto;
import com.boris.weatherapi.dto.CityWeatherDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final WebClient webClient;
    private final CityPropertiesConfig cityPropertiesConfig;

    public Mono<CitySearchDto> fetchLatitudeAndLongitude(String cityName) {
        var url = "%s?q=%s&format=json".formatted(cityPropertiesConfig.getSearchApi(), cityName);

        return (webClient.get()
                        .uri(url)
                        .retrieve()
                        .onStatus(
                                status -> !status.is2xxSuccessful(),
                                clientResponse -> clientResponse.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(new RuntimeException("HTTP Error: " + body)))
                        )
                )
                .bodyToFlux(CitySearchDto.class)
                .next()
                .switchIfEmpty(Mono.error(
                        new RuntimeException("No city found for " + cityName)
                ))
                .doOnError(e ->
                        log.error("City search failed for {}: {}", cityName, e.getMessage())
                );
    }

    public Mono<CityWeatherDto> fetchWeather(String latitude, String longitude) {
        var url = "%s?latitude=%s&longitude=%s&current_weather=true"
                .formatted(cityPropertiesConfig.getWeatherApi(), latitude, longitude);
        log.info("Fetching weather for city {}: {}", latitude, longitude);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("HTTP Error: " + body)))
                )
                .bodyToMono(CityWeatherDto.class);
    }

}
