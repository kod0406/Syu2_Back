package com.example.demo.recommendation.dto;

import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.recommendation.enums.WeatherType;
import com.example.demo.recommendation.enums.SeasonType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherMenuSuggestion {
    private WeatherType weatherType;
    private SeasonType season;
    private List<MenuCategory> recommendedCategories;
    private String reason;
    private Double temperature;
    private Integer humidity;
    private String suggestion;
    private Integer priority;
    private List<String> keywords;
}
