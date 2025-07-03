package com.example.demo.recommendation.service;

import com.example.demo.external.gemini.service.GeminiApiService;
import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.recommendation.dto.MenuAnalysisResult;
import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.MenuRecommendationCache;
import com.example.demo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MenuRecommendationService {
    private final LocationWeatherService locationWeatherService;
    private final ReviewAnalyzer reviewAnalyzer;
    private final WeatherMenuAnalyzer weatherMenuAnalyzer;
    private final GeminiApiService geminiApiService;
    private final RecommendationCacheService cacheService;
    private final StoreRepository storeRepository;

    // 메인 추천 생성 (StoreLocation 기반)
    public MenuRecommendationResponse generateRecommendation(Long storeId) {
        // 1. 캐시 확인
        Optional<MenuRecommendationResponse> cached = cacheService.getCachedRecommendation(storeId);
        if (cached.isPresent()) {
            log.info("Cache hit for store: {}", storeId);
            return cached.get();
        }
        // 2. 새로운 추천 생성
        log.info("Cache miss, generating new recommendation for store: {}", storeId);
        return generateNewRecommendation(storeId);
    }

    // 캐시 무시하고 새로운 추천 생성 (public 메서드로 변경)
    public MenuRecommendationResponse generateNewRecommendation(Long storeId) {
        // 1. StoreLocation 기반 날씨 정보 수집
        StoreWeatherInfo weatherInfo = locationWeatherService.getStoreWeatherInfo(storeId);
        // 2. 리뷰 분석
        List<MenuAnalysisResult> menuAnalysis = reviewAnalyzer.analyzeRecentReviews(storeId);
        // 3. 날씨 기반 메뉴 추천
        List<MenuCategory> suggestedCategories = weatherMenuAnalyzer.suggestMenuCategories(
                weatherInfo.getWeatherType(), weatherInfo.getSeason()
        );
        // 4. AI 조언 생성
        String aiAdvice = generateAIAdvice(weatherInfo, menuAnalysis, suggestedCategories);
        // 5. 응답 생성
        MenuRecommendationResponse response = MenuRecommendationResponse.builder()
                .storeId(storeId)
                .weatherInfo(weatherInfo)
                .menuAnalysis(menuAnalysis)
                .suggestedCategories(suggestedCategories)
                .aiAdvice(aiAdvice)
                .generatedAt(LocalDateTime.now())
                .fromCache(false)
                .build();
        // 6. 캐시 저장
        cacheService.saveRecommendation(response);
        return response;
    }

    private String generateAIAdvice(StoreWeatherInfo weatherInfo,
                                    List<MenuAnalysisResult> menuAnalysis,
                                    List<MenuCategory> suggestedCategories) {
        String prompt = buildAIPrompt(weatherInfo, menuAnalysis, suggestedCategories);
        try {
            return geminiApiService.generateMenuRecommendation(prompt)
                    .block();
        } catch (Exception e) {
            log.error("Gemini API error", e);
            return "AI 서비스 일시 장애로 기본 추천을 제공합니다. 현재 날씨에 맞는 따뜻한 메뉴를 준비해보세요.";
        }
    }

    private String buildAIPrompt(StoreWeatherInfo weatherInfo,
                                 List<MenuAnalysisResult> menuAnalysis,
                                 List<MenuCategory> suggestedCategories) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("음식점 사장님을 위한 메뉴 추천 조언을 생성해주세요.\n\n");
        // 위치 및 날씨 정보
        prompt.append("📍 상점 위치: ").append(weatherInfo.getLocationSummary()).append("\n");
        prompt.append("🌤️ 현재 날씨: ").append(weatherInfo.getWeatherSummary()).append("\n");
        prompt.append("🗓️ 계절: ").append(weatherInfo.getSeason().getKorean()).append("\n\n");
        // 메뉴 분석 결과
        prompt.append("📊 최근 7일 메뉴 분석:\n");
        menuAnalysis.forEach(menu -> {
            prompt.append("- ").append(menu.getMenuName())
                  .append(": ").append(menu.getAverageRating()).append("★")
                  .append(" (리뷰 ").append(menu.getReviewCount()).append("개)\n");
        });
        // 날씨 기반 추천 카테고리
        prompt.append("\n🎯 날씨 맞춤 추천 카테고리: ");
        prompt.append(suggestedCategories.stream()
                .map(MenuCategory::getKorean)
                .collect(Collectors.joining(", ")));
        prompt.append("\n\n");
        prompt.append("위 정보를 바탕으로 음식점 사장님께 다음 3가지 조언을 해주세요:\n");
        prompt.append("1. 오늘 날씨에 맞는 메뉴 추천 및 홍보 전략\n");
        prompt.append("2. 인기 메뉴 활용 방안\n");
        prompt.append("3. 평점이 낮은 메뉴 개선 제안\n\n");
        prompt.append("친근하고 실용적인 조언으로 작성해주세요. 각 조언은 2-3문장으로 간결하게 부탁합니다.");
        return prompt.toString();
    }

    // 추천 히스토리 조회
    public List<MenuRecommendationCache> getRecommendationHistory(Long storeId, LocalDateTime since) {
        try {
            Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

            return cacheService.getRecommendationHistory(store, since);
        } catch (Exception e) {
            log.error("Error getting recommendation history for store: {}", storeId, e);
            return new ArrayList<>();
        }
    }
}
