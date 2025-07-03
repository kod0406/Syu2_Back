package com.example.demo.recommendation.dto;

import com.example.demo.external.weather.dto.WeatherResponse;
import com.example.demo.recommendation.enums.SeasonType;
import com.example.demo.recommendation.enums.WeatherType;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreLocation;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreWeatherInfo {
    private Store store;
    private StoreLocation location;
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

    // 위치 요약 정보
    public String getLocationSummary() {
        return String.format("%s %s", location.getCity(), location.getDistrict());
    }
}

