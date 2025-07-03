package com.example.demo.recommendation.dto;

import com.example.demo.recommendation.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRecommendationResponse {
    private Long storeId;
    private StoreWeatherInfo weatherInfo;
    private List<MenuAnalysisResult> menuAnalysis;
    private List<MenuCategory> suggestedCategories;
    private String aiAdvice;
    private LocalDateTime generatedAt;
    private boolean fromCache;
    private String errorMessage;
}
