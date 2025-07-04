package com.example.demo.recommendation.dto;

import com.example.demo.recommendation.enums.MenuCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // 프론트엔드 편의를 위한 추가 필드들
    private boolean hasMenuData;
    private boolean hasReviewData;
    private String weatherSummary;
    private String locationSummary;
    private int totalMenuCount;
    private int totalReviewCount;

    @JsonIgnore
    public void enrichForFrontend() {
        this.hasMenuData = menuAnalysis != null && !menuAnalysis.isEmpty();
        this.hasReviewData = menuAnalysis != null && menuAnalysis.stream()
            .anyMatch(menu -> menu.getReviewCount() > 0);

        if (weatherInfo != null) {
            this.weatherSummary = weatherInfo.getWeatherSummary();
            this.locationSummary = weatherInfo.getLocationSummary();
        }

        this.totalMenuCount = menuAnalysis != null ? menuAnalysis.size() : 0;
        this.totalReviewCount = menuAnalysis != null ?
            menuAnalysis.stream().mapToInt(MenuAnalysisResult::getReviewCount).sum() : 0;
    }
}
