package com.example.demo.recommendation.controller;

import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.recommendation.dto.RecommendationHistoryResponse;
import com.example.demo.recommendation.service.LocationWeatherService;
import com.example.demo.recommendation.service.MenuRecommendationService;
import com.example.demo.store.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<MenuRecommendationResponse> getCurrentRecommendation(
            @PathVariable Long storeId,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse(storeId, "접근 권한이 없습니다."));
        }

        try {
            MenuRecommendationResponse recommendation = recommendationService.generateRecommendation(storeId);

            if (recommendation != null) {
                return ResponseEntity.ok(recommendation);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(storeId, "추천 생성에 실패했습니다."));
            }
        } catch (Exception e) {
            log.error("Error getting recommendation for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(storeId, "추천 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<MenuRecommendationResponse> refreshRecommendation(
            @PathVariable Long storeId,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse(storeId, "접근 권한이 없습니다."));
        }

        try {
            MenuRecommendationResponse recommendation = recommendationService.forceGenerateNewRecommendation(storeId);

            if (recommendation != null) {
                return ResponseEntity.ok(recommendation);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(storeId, "추천 갱신에 실패했습니다."));
            }
        } catch (Exception e) {
            log.error("Error refreshing recommendation for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(storeId, "추천 갱신 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/weather")
    public ResponseEntity<StoreWeatherInfo> getStoreWeather(
            @PathVariable Long storeId,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StoreWeatherInfo weatherInfo = locationWeatherService.getStoreWeatherInfo(storeId);
            return ResponseEntity.ok(weatherInfo);
        } catch (Exception e) {
            log.error("Error getting weather for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 추천 히스토리 조회 (정규식 처리된 aiAdvice 포함)
    @GetMapping("/history")
    public ResponseEntity<List<RecommendationHistoryResponse>> getRecommendationHistory(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (days < 1 || days > 365) {
            log.warn("Invalid days parameter: {} for store: {}", days, storeId);
            return ResponseEntity.badRequest().build();
        }

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<RecommendationHistoryResponse> history =
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
            .fromCache(false)
            .build();
    }
}