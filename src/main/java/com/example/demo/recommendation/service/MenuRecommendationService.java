package com.example.demo.recommendation.service;

import com.example.demo.external.gemini.service.GeminiApiService;
import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.dto.RecommendationHistoryResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.recommendation.dto.MenuAnalysisResult;
import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.store.entity.MenuRecommendationCache;
import com.example.demo.store.entity.MenuRecommendationHistory;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.recommendation.repository.MenuRecommendationCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MenuRecommendationCacheRepository cacheRepository;

    // 메인 추천 생성 (StoreLocation 기반)
    public MenuRecommendationResponse generateRecommendation(Long storeId) {
        // 1. 진행 중인 요청 확인 (동시 요청 방지)
        String processingKey = "processing_recommendation:" + storeId;
        Boolean isProcessing = redisTemplate.opsForValue().setIfAbsent(processingKey, "true", Duration.ofMinutes(2));

        if (!isProcessing) {
            log.info("Recommendation generation already in progress for store: {}, returning cached result", storeId);
            // 진행 중이면 기존 캐시 반환 (없으면 잠시 대기 후 재시도)
            try {
                Thread.sleep(1000); // 1초 대기
                Optional<MenuRecommendationResponse> cached = cacheService.getCachedRecommendation(storeId);
                if (cached.isPresent()) {
                    return cached.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            // 2. 캐시 확인
            Optional<MenuRecommendationResponse> cached = cacheService.getCachedRecommendation(storeId);
            if (cached.isPresent()) {
                log.info("Cache hit for store: {}", storeId);
                return cached.get();
            }
            // 3. 새로운 추천 생성
            log.info("Cache miss, generating new recommendation for store: {}", storeId);
            return generateNewRecommendation(storeId);
        } finally {
            // 처리 완료 후 락 해제
            redisTemplate.delete(processingKey);
        }
    }

    // 캐시 무시 새로운 추천 생성 (public 메서드로 변경)
    public MenuRecommendationResponse generateNewRecommendation(Long storeId) {
        return generateNewRecommendation(storeId, false);
    }

    // 강제 새 추천 생성 (refresh용)
    public MenuRecommendationResponse forceGenerateNewRecommendation(Long storeId) {
        return generateNewRecommendation(storeId, true);
    }

    private MenuRecommendationResponse generateNewRecommendation(Long storeId, boolean forceRefresh) {
        // forceRefresh가 true가 아닐 때만 최근 캐시 확인
        if (!forceRefresh) {
            // 1. 최근 캐시 확인 (너무 자주 새로고침 방지)
            Optional<MenuRecommendationResponse> recentCache = checkRecentCache(storeId);
            if (recentCache.isPresent()) {
                log.info("Recent cache found for store: {}, skipping new generation", storeId);
                MenuRecommendationResponse cached = recentCache.get();
                cached.setFromCache(true);
                return cached;
            }
        } else {
            log.info("Force refresh requested for store: {}, bypassing cache", storeId);
        }

        // 2. StoreLocation 기반 날씨 정보 수집
        StoreWeatherInfo weatherInfo = locationWeatherService.getStoreWeatherInfo(storeId);
        // 3. 리뷰 분석
        List<MenuAnalysisResult> menuAnalysis = reviewAnalyzer.analyzeRecentReviews(storeId);
        // 4. 날씨 기반 메뉴 추천
        List<MenuCategory> suggestedCategories = weatherMenuAnalyzer.suggestMenuCategories(
                weatherInfo.getWeatherType(), weatherInfo.getSeason()
        );
        // 5. AI 조언 생성
        String aiAdvice = generateAIAdvice(weatherInfo, menuAnalysis, suggestedCategories);
        // 6. 응답 생성
        MenuRecommendationResponse response = MenuRecommendationResponse.builder()
                .storeId(storeId)
                .weatherInfo(weatherInfo)
                .menuAnalysis(menuAnalysis)
                .suggestedCategories(suggestedCategories)
                .aiAdvice(aiAdvice)
                .generatedAt(LocalDateTime.now())
                .fromCache(false)
                .build();
        // 7. 캐시 저장
        cacheService.saveRecommendation(response);
        return response;
    }

    private String generateAIAdvice(StoreWeatherInfo weatherInfo,
                                    List<MenuAnalysisResult> menuAnalysis,
                                    List<MenuCategory> suggestedCategories) {
        String prompt = buildAIPrompt(weatherInfo, menuAnalysis, suggestedCategories);

        // 프롬프트 로깅 - 어떤 프롬프트를 제미나이에게 보내는지 출력
        log.info("=== AI 프롬프트 로그 (Store ID: {}) ===", weatherInfo.getStoreId());
        log.info("데이터 상황: 메뉴분석={}, 추천카테고리={}",
            menuAnalysis != null ? menuAnalysis.size() + "개" : "없음",
            suggestedCategories != null ? suggestedCategories.size() + "개" : "없음");
        log.info("프롬프트 내용:\n{}", prompt);
        log.info("=== 프롬프트 로그 끝 ===");

        try {
            String rawResponse = geminiApiService.generateMenuRecommendation(prompt)
                    .block();

            // AI 응답 로깅
            log.info("=== AI 응답 로그 (Store ID: {}) ===", weatherInfo.getStoreId());
            log.info("원본 응답:\n{}", rawResponse);

            String formattedResponse = formatAIResponse(rawResponse);
            log.info("포맷팅된 응답:\n{}", formattedResponse);
            log.info("=== AI 응답 로그 끝 ===");

            return formattedResponse;
        } catch (Exception e) {
            log.error("Gemini API error for store: {}", weatherInfo.getStoreId(), e);
            return "AI 서비스 일시 장애로 기본 추천을 제공합니다. 현재 날씨에 맞는 따뜻한 메뉴를 준비해보세요.";
        }
    }

    // AI 응답을 구조화된 형태로 정리 (HTML 포맷팅 적용)
    private String formatAIResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return "AI 응답을 처리할 수 없습니다.";
        }

        try {
            // 기본 정리
            String cleanedResponse = rawResponse
                .replaceAll("(?i)사장님,?\\s*안녕하세요[!.]?[\\s\\n]*", "")
                .replaceAll("(?i)안녕하세요,?\\s*사장님[!.]?[\\s\\n]*", "")
                .replaceAll("(?i)화이팅[!]*[\\s\\n]*$", "")
                .replaceAll("(?i)응원하겠습니다[!]*[\\s\\n]*$", "")
                .trim();

            // HTML 포맷팅 적용
            return formatAiAdviceWithRegex(cleanedResponse);

        } catch (Exception e) {
            log.error("AI response formatting error", e);
            return rawResponse; // 포맷팅 실패 시 원본 반환
        }
    }

    // AI 조언 HTML 포맷팅 메서드 (다시 활성화)
    private String formatAiAdviceWithRegex(String aiAdvice) {
        if (aiAdvice == null || aiAdvice.trim().isEmpty()) {
            return aiAdvice;
        }

        return aiAdvice
            // 1. **텍스트** -> <strong>텍스트</strong>
            .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
            // 2. *텍스트* -> <em>텍스트</em>
            .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
            // 3. 줄바꿈을 <br> 태그로 변환
            .replaceAll("\\n", "<br>")
            // 4. 번호 목록 처리 (1. 2. 3. ...)
            .replaceAll("(\\d+\\.)\\s", "<br><strong>$1</strong> ")
            // 5. 불필요한 연속된 <br> 정리
            .replaceAll("(<br>){3,}", "<br><br>")
            // 6. 시작 부분의 <br> 제거
            .replaceAll("^<br>+", "");
    }

    private String buildAIPrompt(StoreWeatherInfo weatherInfo,
                                 List<MenuAnalysisResult> menuAnalysis,
                                 List<MenuCategory> suggestedCategories) {
        StringBuilder prompt = new StringBuilder();

        // 더 구체적인 프롬프트 헤더 - 불필요한 인사말 방지
        prompt.append("당신은 전문 음식점 경영 컨설턴트입니다.\n");
        prompt.append("아래 매장 정보를 분석하여 즉시 실행 가능한 경영 조언 3가지를 제시하세요.\n\n");

        prompt.append("**출력 형식 규칙:**\n");
        prompt.append("- 인사말이나 자기소개 절대 포함하지 마세요\n");
        prompt.append("- '💡', '경영 컨설턴트 AI', '힘이 될 만한' 등의 표현 사용 금지\n");
        prompt.append("- 각 조언은 구체적인 행동 방안으로 바로 시작하세요\n");
        prompt.append("- 번호와 함께 **굵게** 제목을 달고, 2-3문장으로 간결하게 작성하세요\n\n");

        // 위치 및 날씨 정보
        prompt.append("**매장 현황 정보:**\n");
        prompt.append("- 위치: ").append(weatherInfo.getLocationSummary()).append("\n");
        prompt.append("- 날씨: ").append(weatherInfo.getWeatherSummary()).append("\n");
        prompt.append("- 계절: ").append(weatherInfo.getSeason().getKorean()).append("\n\n");

        // 메뉴 분석 결과에 따른 차별화된 프롬프트
        if (menuAnalysis == null || menuAnalysis.isEmpty()) {
            prompt.append("**매장 상황:** 메뉴 데이터 없음 (신규 매장 또는 데이터 부족)\n\n");

            prompt.append("**요청사항:** 다음 3가지 조언을 구체적으로 제시하세요:\n");
            prompt.append("1. **오늘 날씨 맞춤 메뉴 준비** - 현재 ").append(weatherInfo.getWeatherSummary())
                  .append(" 날씨에 고객이 선호할 메뉴와 준비 방법\n");
            prompt.append("2. **초기 매장 운영 전략** - 메뉴 구성과 고객 유치 방안\n");
            prompt.append("3. **리뷰 수집 방법** - 고객 피드백을 빠르게 모으는 실용적 방법\n\n");

        } else if (menuAnalysis.size() < 3) {
            prompt.append("**매장 상황:** 제한적 메뉴 데이터 (").append(menuAnalysis.size()).append("개 메뉴)\n");
            menuAnalysis.forEach(menu -> {
                prompt.append("- ").append(menu.getMenuName());
                if (menu.getReviewCount() == 0) {
                    prompt.append(": 리뷰 없음\n");
                } else {
                    prompt.append(": ").append(menu.getAverageRating()).append("★ (")
                          .append(menu.getReviewCount()).append("리뷰)\n");
                }
            });
            prompt.append("\n");

            prompt.append("**요청사항:** 다음 3가지 조언을 구체적으로 제시하세요:\n");
            prompt.append("1. **날씨 활용 메뉴 홍보** - ").append(weatherInfo.getWeatherSummary())
                  .append(" 날씨에 기존 메뉴를 어떻게 어필할지\n");
            prompt.append("2. **메뉴 확장 전략** - 현재 메뉴를 기반으로 한 신메뉴 개발 방향\n");
            prompt.append("3. **고객 참여 증대** - 리뷰와 재방문을 늘리는 구체적 방법\n\n");

        } else {
            prompt.append("**매장 상황:** 충분한 메뉴 데이터 (").append(menuAnalysis.size()).append("개 메뉴)\n");
            menuAnalysis.forEach(menu -> {
                prompt.append("- ").append(menu.getMenuName())
                      .append(": ").append(menu.getAverageRating()).append("★ (")
                      .append(menu.getReviewCount()).append("리뷰)\n");
            });
            prompt.append("\n");

            // 인기/비인기 메뉴 분석
            MenuAnalysisResult topMenu = menuAnalysis.stream()
                .filter(m -> m.getReviewCount() > 0)
                .max((a, b) -> Double.compare(a.getAverageRating(), b.getAverageRating()))
                .orElse(null);
            MenuAnalysisResult lowMenu = menuAnalysis.stream()
                .filter(m -> m.getReviewCount() > 0)
                .min((a, b) -> Double.compare(a.getAverageRating(), b.getAverageRating()))
                .orElse(null);

            prompt.append("**요청사항:** 다음 3가지 조언을 구체적으로 제시하세요:\n");
            prompt.append("1. **날씨 맞춤 메뉴 추천** - ").append(weatherInfo.getWeatherSummary())
                  .append(" 날씨에 어떤 메뉴를 중점 판매할지\n");
            if (topMenu != null) {
                prompt.append("2. **인기 메뉴 활용** - ").append(topMenu.getMenuName())
                      .append("(").append(topMenu.getAverageRating()).append("★) 메뉴 마케팅 방법\n");
            } else {
                prompt.append("2. **메뉴 마케팅 강화** - 기존 메뉴의 매력도를 높이는 방법\n");
            }
            if (lowMenu != null && lowMenu.getAverageRating() < 4.0) {
                prompt.append("3. **저평점 메뉴 개선** - ").append(lowMenu.getMenuName())
                      .append("(").append(lowMenu.getAverageRating()).append("★) 메뉴 개선 방안\n");
            } else {
                prompt.append("3. **매출 극대화 전략** - 현재 메뉴 라인업으로 수익을 늘리는 방법\n");
            }
            prompt.append("\n");
        }

        // 날씨 기반 추천 카테고리 추가 정보
        if (suggestedCategories != null && !suggestedCategories.isEmpty()) {
            prompt.append("**참고 - 날씨 추천 카테고리:** ");
            prompt.append(suggestedCategories.stream()
                    .map(MenuCategory::getKorean)
                    .collect(Collectors.joining(", ")));
            prompt.append("\n\n");
        }

        prompt.append("**주의사항:** 반드시 번호와 함께 각 조언을 제목으로 시작하고, ");
        prompt.append("즉시 실행 가능한 구체적인 방법만 제시하세요. 추상적이거나 일반적인 조언은 금지합니다.");

        return prompt.toString();
    }

    // 추천 히스토리 조회
    public List<RecommendationHistoryResponse> getRecommendationHistory(Long storeId, LocalDateTime since) {
        try {
            Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

            return cacheService.getRecommendationHistory(store, since);
        } catch (Exception e) {
            log.error("Error getting recommendation history for store: {}", storeId, e);
            return new ArrayList<>();
        }
    }

    // 최근 캐시 확인 (5분 이내 생성된 캐시가 있으면 재사용)
    private Optional<MenuRecommendationResponse> checkRecentCache(Long storeId) {
        try {
            // Redis에서 먼저 확인
            String cacheKey = generateRecentCacheKey(storeId);
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null) {
                return Optional.of(objectMapper.readValue(cachedJson, MenuRecommendationResponse.class));
            }

            // DB에서 5분 이내 캐시 확인
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            return cacheRepository.findRecentCacheByStoreId(storeId, fiveMinutesAgo)
                .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error checking recent cache for store: {}", storeId, e);
            return Optional.empty();
        }
    }

    // DB 캐시를 Response로 변환
    private MenuRecommendationResponse convertToResponse(MenuRecommendationCache cache) {
        try {
            return objectMapper.readValue(cache.getGptRecommendation(), MenuRecommendationResponse.class);
        } catch (Exception e) {
            log.error("DB cache parsing error for cache id: {}", cache.getId(), e);
            return null;
        }
    }

    // 최근 캐시용 키 생성 (5분 단위)
    private String generateRecentCacheKey(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        int fiveMinuteBlock = (now.getMinute() / 5) * 5; // 5분 단위로 반올림
        return String.format("recent_recommendation:%d:%d:%d:%d",
            storeId, now.getDayOfYear(), now.getHour(), fiveMinuteBlock);
    }
}
