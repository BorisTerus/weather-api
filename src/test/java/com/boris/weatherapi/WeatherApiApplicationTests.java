package com.boris.weatherapi;

import com.boris.weatherapi.dto.CitySearchDto;
import com.boris.weatherapi.dto.CityWeatherDto;
import com.boris.weatherapi.dto.WeatherResponseDto;
import com.boris.weatherapi.service.ExternalApiService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherApiApplicationTests {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExternalApiService externalApiService;

    @Autowired
    private RedisCacheManager cacheManager;

    private WeatherResponseDto getWeatherResponse() {
        return new WeatherResponseDto(1d, 2d, "N");
    }

    @Test
    @SneakyThrows
    @Order(1)
    void should_Succeed_AndCacheWeather() {
        //Arrange
        when(externalApiService.fetchLatitudeAndLongitude(anyString()))
                .thenReturn(Mono.just(new CitySearchDto("1", "2")));
        when(externalApiService.fetchWeather(anyString(), anyString()))
                .thenReturn(Mono.just(new CityWeatherDto(getWeatherResponse())));
        //Act
        mockMvc.perform(get("/weather?city=London")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("USER-ID", "test"))
                .andExpect(status().isOk());

        //Assert
        Assertions.assertTrue(cacheManager.getCacheNames().contains("weather-cache"));
        var cache = cacheManager.getCache("weather-cache");
        WeatherResponseDto londonWeather = cache.get("london", WeatherResponseDto.class);
        assertAll(
                () -> Assertions.assertEquals(londonWeather.temperature(), getWeatherResponse().temperature()),
                () -> Assertions.assertEquals(londonWeather.windspeed(), getWeatherResponse().windspeed()),
                () -> Assertions.assertEquals(londonWeather.winddirection(), getWeatherResponse().winddirection())
        );
    }

    @Test
    @Order(2)
    @SneakyThrows
    void should_Fail_WhenRateLimitExceeded() {
        //Arrange
        when(externalApiService.fetchLatitudeAndLongitude(anyString()))
                .thenReturn(Mono.just(new CitySearchDto("1", "2")));
        when(externalApiService.fetchWeather(anyString(), anyString()))
                .thenReturn(Mono.just(new CityWeatherDto(getWeatherResponse())));
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/weather")
                            .param("city", "London")
                            .header("USER-ID", "test1"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/weather")
                        .param("city", "London")
                        .header("USER-ID", "test1"))
                .andExpect(status().isTooManyRequests());
    }

}
