package com.example.demo.recommendation.controller;

import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.recommendation.service.LocationWeatherService;
import com.example.demo.recommendation.service.MenuRecommendationService;
import com.example.demo.store.entity.MenuRecommendationCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/recommendations")
@Slf4j
@RequiredArgsConstructor
public class MenuRecommendationController {
    private final MenuRecommendationService recommendationService;
    private final LocationWeatherService locationWeatherService;

    // 현재 추천 조회 (위치 정보 포함)
    @GetMapping
    public ResponseEntity<MenuRecommendationResponse> getCurrentRecommendation(
            @PathVariable Long storeId) {
        try {
            MenuRecommendationResponse recommendation = recommendationService.generateRecommendation(storeId);
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            log.error("Error getting recommendation for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(storeId, "추천 조회 중 오류가 발생했습니다."));
        }
    }

    // 수동 추천 갱신
    @PostMapping("/refresh")
    public ResponseEntity<MenuRecommendationResponse> refreshRecommendation(
            @PathVariable Long storeId) {
        try {
            // 캐시 무시하고 새로 생성
            MenuRecommendationResponse recommendation = recommendationService.generateNewRecommendation(storeId);
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            log.error("Error refreshing recommendation for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(storeId, "추천 갱신 중 오류가 발생했습니다."));
        }
    }

    // 매장 위치 및 날씨 정보만 조회
    @GetMapping("/weather")
    public ResponseEntity<StoreWeatherInfo> getStoreWeather(@PathVariable Long storeId) {
        try {
            StoreWeatherInfo weatherInfo = locationWeatherService.getStoreWeatherInfo(storeId);
            return ResponseEntity.ok(weatherInfo);
        } catch (Exception e) {
            log.error("Error getting weather for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 추천 히스토리 조회
    @GetMapping("/history")
    public ResponseEntity<List<MenuRecommendationCache>> getRecommendationHistory(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            // days일 전부터의 히스토리 조회
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<MenuRecommendationCache> history =
                recommendationService.getRecommendationHistory(storeId, since);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting recommendation history for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MenuRecommendationResponse createErrorResponse(Long storeId, String message) {
        return MenuRecommendationResponse.builder()
            .storeId(storeId)
            .aiAdvice(message)
            .generatedAt(LocalDateTime.now())
            .build();
    }
}
