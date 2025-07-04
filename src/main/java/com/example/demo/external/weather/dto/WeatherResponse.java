package com.example.demo.external.weather.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private WeatherMain main;
    private List<WeatherDetails> weather;
    private String name;
    private Coordinates coord;
    private int visibility;
    private Wind wind;
    private Clouds clouds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wind {
        private double speed;
        private int deg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clouds {
        private int all;
    }
}
