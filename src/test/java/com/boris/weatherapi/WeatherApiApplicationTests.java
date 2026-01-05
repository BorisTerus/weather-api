package com.boris.weatherapi;

import com.boris.weatherapi.dto.CitySearchDto;
import com.boris.weatherapi.dto.CityWeatherDto;
import com.boris.weatherapi.dto.WeatherResponseDto;
import com.boris.weatherapi.service.ExternalApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    // Override spring.redis properties to point to Testcontainers Redis
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

    @BeforeEach
    void beforeEach() {
        when(externalApiService.fetchLatitudeAndLongitude(anyString()))
                .thenReturn(new CitySearchDto("1", "2"));
        when(externalApiService.fetchWeather(anyString(), anyString()))
                .thenReturn(new CityWeatherDto(getWeatherResponse()));

    }

    private WeatherResponseDto getWeatherResponse() {
        return new WeatherResponseDto(1d, 2d, "N");
    }

    @Test
    @SneakyThrows
    @Order(1)
    void should_Succeed_AndCacheWeather() {

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

}
