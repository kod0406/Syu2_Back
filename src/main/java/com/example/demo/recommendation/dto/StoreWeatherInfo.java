package com.example.demo.recommendation.dto;

import com.example.demo.external.weather.dto.WeatherResponse;
import com.example.demo.recommendation.enums.SeasonType;
import com.example.demo.recommendation.enums.WeatherType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StoreWeatherInfo {
    // Store 엔티티 대신 필요한 정보만 포함
    private Long storeId;
    private String storeName;

    // StoreLocation 엔티티 대신 필요한 정보만 포함
    private String fullAddress;  // 전체 주소 추가
    private String city;
    private String district;

    private WeatherResponse weather;
    private WeatherType weatherType;
    private SeasonType season;

    // 날씨 요약 정보
    public String getWeatherSummary() {
        return String.format("%s, %.1f°C (체감 %.1f°C)",
            weather.getWeather().get(0).getDescription(),
            weather.getMain().getTemp(),
            weather.getMain().getFeels_like());
    }

    // 온도 정보 반환
    public Double getTemperature() {
        return weather != null && weather.getMain() != null ? weather.getMain().getTemp() : null;
    }

    // 습도 정보 반환
    public Integer getHumidity() {
        return weather != null && weather.getMain() != null ? weather.getMain().getHumidity() : null;
    }

    // 위치 요약 정보 - 전체 주소 반환
    public String getLocationSummary() {
        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
            return fullAddress;
        }
        // fullAddress가 없는 경우 기존 방식으로 fallback
        return String.format("%s %s", city, district);
    }
}
