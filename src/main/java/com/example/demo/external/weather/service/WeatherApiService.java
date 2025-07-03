package com.example.demo.external.weather.service;

import com.example.demo.external.weather.dto.WeatherDetails;
import com.example.demo.external.weather.dto.WeatherForecastResponse;
import com.example.demo.external.weather.dto.WeatherMain;
import com.example.demo.external.weather.dto.WeatherResponse;
import com.example.demo.recommendation.enums.WeatherType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherApiService {
    private final WebClient webClient;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String baseUrl;

    // 위도/경도로 현재 날씨 조회
    public Mono<WeatherResponse> getCurrentWeather(double lat, double lon) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/weather")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "kr")
                .build())
            .retrieve()
            .bodyToMono(WeatherResponse.class)
            .doOnSuccess(response -> log.info("Weather API success for lat: {}, lon: {}", lat, lon))
            .doOnError(error -> log.error("Weather API error for lat: {}, lon: {}", lat, lon, error))
            .onErrorResume(throwable -> {
                log.error("Weather API fallback triggered", throwable);
                return Mono.just(createFallbackWeather(lat, lon));
            });
    }

    // 5일 예보 조회 (추가 기능)
    public Mono<WeatherForecastResponse> getForecast(double lat, double lon) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/forecast")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "kr")
                .build())
            .retrieve()
            .bodyToMono(WeatherForecastResponse.class);
    }

    // API 장애 시 기본 날씨 데이터
    private WeatherResponse createFallbackWeather(double lat, double lon) {
        // 기본 맑은 날씨 데이터 반환
        return WeatherResponse.builder()
            .main(WeatherMain.builder()
                .temp(20.0)
                .feels_like(20.0)
                .humidity(50)
                .build())
            .weather(List.of(WeatherDetails.builder()
                .main("Clear")
                .description("맑음")
                .build()))
            .name("Unknown")
            .build();
    }

    // 날씨 조건을 WeatherType으로 변환
    public WeatherType determineWeatherType(WeatherResponse response) {
        String main = response.getWeather().get(0).getMain().toLowerCase();
        double temp = response.getMain().getTemp();
        int humidity = response.getMain().getHumidity();

        // 온도 기반 판단
        if (temp <= 5) return WeatherType.COLD;
        if (temp >= 30) return WeatherType.HOT;

        // 날씨 상태 기반 판단
        if (main.contains("rain")) return WeatherType.RAIN;
        if (main.contains("snow")) return WeatherType.SNOW;
        if (main.contains("cloud")) return WeatherType.CLOUDS;
        if (main.contains("thunder")) return WeatherType.THUNDERSTORM;
        if (main.contains("drizzle")) return WeatherType.DRIZZLE;
        if (main.contains("mist") || main.contains("fog")) return WeatherType.MIST;
        if (main.contains("haze")) return WeatherType.HAZE;
        if (main.contains("dust")) return WeatherType.DUST;

        // 습도 기반 추가 판단
        if (humidity > 80) return WeatherType.HUMID;
        if (humidity < 30) return WeatherType.DRY;

        // 기본값
        return WeatherType.CLEAR;
    }
}
