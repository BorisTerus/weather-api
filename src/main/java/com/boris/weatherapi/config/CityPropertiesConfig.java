package com.boris.weatherapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "city")
@Getter
public class CityPropertiesConfig {

    @Value("${city.search.api}")
    private String searchApi;

    @Value("${city.weather.api}")
    private String weatherApi;

}
