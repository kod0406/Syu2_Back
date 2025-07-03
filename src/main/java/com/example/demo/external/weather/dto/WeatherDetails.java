package com.example.demo.external.weather.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDetails {
    private int id;
    private String main;
    private String description;
    private String icon;
}
