package com.example.demo.recommendation.service;

import com.example.demo.external.gemini.service.GeminiApiService;
import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.dto.RecommendationHistoryResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.recommendation.dto.MenuAnalysisResult;
import com.example.demo.recommendation.enums.MenuCategory;
import com.example.demo.recommendation.enums.SeasonType;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.customer.entity.CustomerReviewCollect;
import com.example.demo.customer.repository.CustomerReviewCollectRepository;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.store.repository.StoreMenuRepository;
import com.example.demo.recommendation.repository.MenuRecommendationCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final StoreMenuRepository storeMenuRepository;
    private final RedisTemplate redisTemplate;
    private final CustomerReviewCollectRepository customerReviewRepository;

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
        // forceRefresh가 true일 때만 캐시 무시하고 새로 생성
        if (!forceRefresh) {
            // 60분 기본 캐시 확인 - 있으면 재사용
            Optional<MenuRecommendationResponse> cached = cacheService.getCachedRecommendation(storeId);
            if (cached.isPresent()) {
                log.info("Cache found for store: {}, returning cached result (60min cache)", storeId);
                MenuRecommendationResponse cachedResponse = cached.get();
                cachedResponse.setFromCache(true);
                return cachedResponse;
            }
        } else {
            log.info("Force refresh requested for store: {}, bypassing all cache", storeId);
        }

        // 캐시가 없거나 forceRefresh=true인 경우에만 새로 생성
        log.info("No cache found or force refresh, generating new recommendation for store: {}", storeId);

        // 2. StoreLocation 기반 날씨 정보 수집
        StoreWeatherInfo weatherInfo = locationWeatherService.getStoreWeatherInfo(storeId);

        // 3. 리뷰 분석
        List<MenuAnalysisResult> menuAnalysis = reviewAnalyzer.analyzeRecentReviews(storeId);

        // 4. 메뉴 분석 결과 상세 로그 출력
        log.info("=== [인기 메뉴 분석] Store ID: {} ===", storeId);
        log.info("[인기 메뉴 분석] menuAnalysis 배열 길이: {}", menuAnalysis != null ? menuAnalysis.size() : 0);

        if (menuAnalysis != null && !menuAnalysis.isEmpty()) {
            // 평점 순으로 정렬해서 출력
            List<MenuAnalysisResult> sortedMenus = menuAnalysis.stream()
                    .filter(menu -> menu.getReviewCount() > 0) // 리뷰가 있는 메뉴만
                    .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating())) // 평점 높은 순
                    .collect(Collectors.toList());

            for (int i = 0; i < sortedMenus.size(); i++) {
                MenuAnalysisResult menu = sortedMenus.get(i);
                log.info("[인기 메뉴 분석] 메뉴 {}: {menuName: '{}', averageRating: {}, reviewCount: {}, sentiment: '{}'}",
                        i + 1,
                        menu.getMenuName(),
                        String.format("%.1f", menu.getAverageRating()),
                        menu.getReviewCount(),
                        menu.getSentiment()
                );
            }

            // 리뷰가 없는 메뉴들도 출력
            List<MenuAnalysisResult> noReviewMenus = menuAnalysis.stream()
                    .filter(menu -> menu.getReviewCount() == 0)
                    .collect(Collectors.toList());
            if (!noReviewMenus.isEmpty()) {
                log.info("[인기 메뉴 분석] 리뷰 없는 메뉴: {} 개", noReviewMenus.size());
                noReviewMenus.forEach(menu -> {
                    log.info("[인기 메뉴 분석] - {}: 리뷰 없음", menu.getMenuName());
                });
            }
        } else {
            log.info("[인기 메뉴 분석] 분석 가능한 메뉴 데이터가 없습니다.");
        }
        log.info("=== [인기 메뉴 분석] 끝 ===");

        // 5. 날씨 기반 메뉴 추천
        List<MenuCategory> suggestedCategories = weatherMenuAnalyzer.suggestBestMenuCategories(
                weatherInfo.getWeatherType(),
                weatherInfo.getSeason(),
                weatherInfo.getTemperature(),
                weatherInfo.getHumidity(),
                LocalTime.now(),
                weatherInfo.getWeatherSummary(),
                null, // businessType - 필요시 Store 엔티티에서 추출 가능
                null  // ageGroup - 필요시 리뷰 분석에서 추출 가능
        );

        // 6. AI 조언 생성
        String aiAdvice = generateAIAdvice(weatherInfo, menuAnalysis, suggestedCategories);

        // 7. 응답 생성
        MenuRecommendationResponse response = MenuRecommendationResponse.builder()
                .storeId(storeId)
                .weatherInfo(weatherInfo)
                .menuAnalysis(menuAnalysis)
                .suggestedCategories(suggestedCategories)
                .aiAdvice(aiAdvice)
                .generatedAt(LocalDateTime.now())
                .fromCache(false)
                .build();

        // 8. 캐시 저장
        cacheService.saveRecommendation(response);

        return response;
    }

    private String generateAIAdvice(StoreWeatherInfo weatherInfo,
                                    List<MenuAnalysisResult> menuAnalysis,
                                    List<MenuCategory> suggestedCategories) {

        String prompt = buildOptimizedGeminiPrompt(weatherInfo, menuAnalysis, suggestedCategories);

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
            return "AI 서비스 일시 장애로 기본 추천을 제공합니다. 현재 날씨에 맞는 메뉴를 준비해보세요.";
        }
    }

    // ===== 🔥 완전 개선된 프롬프트 엔지니어링 시작 =====

    /**
     * Gemini 2.0 Flash 최적화된 프롬프트 빌더
     * - Few-shot prompting으로 일관성 확보
     * - Chain of Thought로 분석 품질 향상
     * - 토큰 최적화로 비용 효율성
     * - 실시간 트렌드 반영으로 실용성 증대
     */
    private String buildOptimizedGeminiPrompt(StoreWeatherInfo weatherInfo,
                                              List<MenuAnalysisResult> menuAnalysis,
                                              List<MenuCategory> suggestedCategories) {

        // 토큰 효율성을 위한 조건부 로딩
        boolean hasMenuData = menuAnalysis != null && !menuAnalysis.isEmpty();
        boolean hasReviewData = hasMenuData && menuAnalysis.stream()
                .anyMatch(menu -> menu.getReviewCount() > 0);

        StringBuilder prompt = new StringBuilder();

        // 1. System Context & Role Definition
        prompt.append(buildSystemContext());

        // 2. Few-shot Examples (데이터 상황에 따라 조건부)
        if (hasMenuData) {
            prompt.append(buildAdvancedFewShotExamples());
        } else {
            prompt.append(buildBasicFewShotExamples());
        }

        // 3. Chain of Thought Instructions
        prompt.append(buildChainOfThoughtInstructions());

        // 4. Current Business Context
        prompt.append(buildCurrentBusinessContext(weatherInfo, menuAnalysis, suggestedCategories));

        // 5. Real-time Trend Context
        prompt.append(buildRealtimeTrendContext(weatherInfo));

        // 6. Structured Analysis Request
        prompt.append(buildStructuredAnalysisRequest(weatherInfo, hasMenuData, hasReviewData));

        // 7. Output Format & Quality Constraints
        prompt.append(buildOutputFormatAndConstraints());

        return prompt.toString();
    }

    private String buildSystemContext() {
        return """
                # 음식점 경영 전문 컨설턴트 AI
                
                ## 전문 영역
                - 날씨 기반 고객 행동 패턴 분석
                - 매장 운영 최적화 전략
                - 즉시 실행 가능한 매출 증대 방안
                - 지역별 외식업 트렌드 분석
                
                ## 분석 원칙
                1. 데이터 기반 현실적 판단
                2. 즉시 실행 가능한 구체적 방안
                3. 비용 대비 효과 최우선
                4. 지역 특성 및 날씨 반영 필수
                
                """;
    }

    private String buildAdvancedFewShotExamples() {
        return """
                ## 분석 예시 (메뉴 데이터 충분한 경우)
                
                ### 사례 1: 여름 폭염 + 치킨집 (리뷰 많음)
                **입력 조건**:
                - 날씨: 34°C 폭염, 습도 80%
                - 인기메뉴: 양념치킨 4.5★ (120리뷰), 후라이드 4.2★ (89리뷰)
                - 저조메뉴: 핫윙 3.8★ (15리뷰)
                
                **분석 사고과정**:
                폭염 → 시원한 것 선호 → 차가운 음료 수요 ↑ → 매장 내 시원함 강조 → 아이스크림 디저트 추가 기회
                
                **결과**:
                1. **시원한 매장 어필 즉시 실행**: 에어컨 풀가동 + "시원한 매장" SNS 홍보, 아이스크림 디저트 메뉴 당일 추가
                2. **인기메뉴 여름 버전 개발**: 양념치킨에 시원한 피클 무료 제공, "여름 특별 세트" 즉시 런칭
                3. **저조메뉴 개선**: 핫윙을 "쿨링 윙"으로 리뉴얼, 차가운 소스 개발로 여름용 변신
                
                ### 사례 2: 비오는 날 + 카페 (리뷰 적음)
                **입력 조건**:
                - 날씨: 비 15mm, 쌀쌀함
                - 메뉴: 아메리카노, 라떼, 샌드위치 (리뷰 부족)
                
                **분석 사고과정**:
                비 → 실내 체류시간 ↑ → 따뜻한 음료 선호 → 편안한 분위기 → 할인으로 신규고객 유치
                
                **결과**:
                1. **비오는 날 특가 즉시 실행**: "우산 가져오면 10% 할인" 당일 이벤트
                2. **체류시간 활용**: 무료 WiFi + 콘센트 어필, 학습/업무 공간으로 포지셔닝
                3. **리뷰 수집 집중**: 첫 방문 고객 음료 업그레이드 무료 + 리뷰 작성 유도
                
                """;
    }

    private String buildBasicFewShotExamples() {
        return """
                ## 분석 예시 (신규 매장 또는 데이터 부족한 경우)
                
                ### 사례: 신규 식당 + 여름날씨
                **입력 조건**:
                - 날씨: 30°C 더위
                - 상황: 신규 개업, 메뉴 데이터 없음
                - 지역: 주거지역
                
                **분석 사고과정**:
                신규매장 → 인지도 부족 → 첫인상이 중요 → 날씨 활용한 차별화 → 입소문 중요
                
                **결과**:
                1. **첫인상 강화**: 시원한 매장 환경 + 여름 메뉴 특화로 "더위 피하기 좋은 곳" 포지셔닝
                2. **지역 밀착 마케팅**: 주변 아파트 단지 전단지 배포, 첫 방문 할인 쿠폰
                3. **리뷰 확보 전략**: 첫 100명 고객 특별 이벤트, SNS 인증 시 디저트 무료
                
                """;
    }

    private String buildChainOfThoughtInstructions() {
        return """
                ## 분석 사고 과정 (단계별 수행)
                
                ### 1단계: 환경 분석
                - 현재 날씨 → 고객 심리 변화 예측
                - 시간대 + 계절 → 방문 패턴 분석
                - 지역 특성 → 고객층 특성 파악
                
                ### 2단계: 매장 현황 파악
                - 메뉴 성과 → 강점/약점 식별
                - 리뷰 감정 → 고객 만족도 분석
                - 운영 상황 → 개선 기회 발견
                
                ### 3단계: 기회 발견
                - 환경 + 매장상황 → 즉시 활용 가능한 기회
                - 경쟁사 대비 → 차별화 포인트
                - 단기 수익 → 오늘 당장 매출 기회
                
                ### 4단계: 실행 방안 도출
                - 기회 → 구체적 액션 플랜
                - 비용 → 투자 대비 효과 계산
                - 측정 → 성과 확인 방법
                
                ### 5단계: 우선순위 설정
                - 긴급도 + 중요도 → 실행 순서
                - 리소스 → 현실적 실행 가능성
                - 임팩트 → 예상 효과 크기
                
                """;
    }

    private String buildRealtimeTrendContext(StoreWeatherInfo weatherInfo) {
        StringBuilder context = new StringBuilder();

        context.append("## 실시간 트렌드 반영\n\n");

        // 시간대별 고객 행동 패턴
        LocalTime currentTime = LocalTime.now();
        String timePattern = getTimeBasedCustomerPattern(currentTime);
        context.append("**현재 시간대 특성**: ").append(timePattern).append("\n");

        // 날씨별 고객 심리 분석
        String weatherPsychology = getWeatherBasedPsychology(weatherInfo);
        context.append("**날씨별 고객 심리**: ").append(weatherPsychology).append("\n");

        // 계절별 트렌드
        String seasonalTrend = getSeasonalTrend(weatherInfo.getSeason());
        context.append("**계절 트렌드**: ").append(seasonalTrend).append("\n");

        // 지역별 특성
        String locationTrend = getLocationTrend(weatherInfo.getLocationSummary());
        context.append("**지역 특성**: ").append(locationTrend).append("\n\n");

        return context.toString();
    }

    private String getTimeBasedCustomerPattern(LocalTime currentTime) {
        if (currentTime.isBefore(LocalTime.of(11, 0))) {
            return "아침 시간대 - 가벼운 식사, 커피 수요 높음, 빠른 서비스 선호";
        } else if (currentTime.isBefore(LocalTime.of(14, 0))) {
            return "점심 시간대 - 빠른 식사, 가성비 중시, 회전율 중요";
        } else if (currentTime.isBefore(LocalTime.of(17, 0))) {
            return "오후 시간대 - 여유로운 식사, 디저트/음료 수요, 체류시간 길어짐";
        } else if (currentTime.isBefore(LocalTime.of(21, 0))) {
            return "저녁 시간대 - 풍성한 식사, 가족/친구 모임, 만족도 중시";
        } else {
            return "야간 시간대 - 간단한 야식, 술안주, 매장 내 식사 위주";
        }
    }

    private String getWeatherBasedPsychology(StoreWeatherInfo weatherInfo) {
        String weather = weatherInfo.getWeatherSummary().toLowerCase();

        if (weather.contains("비")) {
            return "실내 체류 욕구 증가, 따뜻한 음식 선호, 편안한 분위기 중시";
        } else if (weather.contains("눈")) {
            return "따뜻함 추구, 뜨거운 음료/찌개류 선호, 특별한 경험 원함";
        } else if (weather.contains("맑음")) {
            return "기분 좋음, 다양한 메뉴 도전, 사진 촬영 욕구 높음";
        } else if (weather.contains("흐림")) {
            return "평범함 선호, 익숙한 메뉴 선택, 할인에 민감";
        } else {
            // 온도 기반 분석
            if (weatherInfo.getWeatherSummary().contains("30") || weatherInfo.getWeatherSummary().contains("3")) {
                return "시원함 추구, 차가운 음료/음식 선호, 에어컨 가동 매장 선호";
            } else {
                return "따뜻함 선호, 뜨거운 음식 선택, 실내 온도 중시";
            }
        }
    }

    private String getSeasonalTrend(SeasonType season) {
        return switch (season) {
            case SPRING -> "신메뉴 출시 시즌, 가벼운 식사 선호, 야외 활동 후 식사 수요";
            case SUMMER -> "시원한 메뉴 각광, 냉음료 필수, 에어컨 가동 매장 선호";
            case AUTUMN -> "따뜻한 음식 회귀, 계절 한정 메뉴 관심, 가족 모임 증가";
            case WINTER -> "뜨거운 음식 선호, 실내 체류시간 증가, 보양식/찌개류 인기";
            default -> "일반적인 계절 특성";
        };
    }

    private String getLocationTrend(String location) {
    // 강남권 (고소득층, 비즈니스 중심)
    if (location.contains("강남") || location.contains("역삼") || location.contains("테헤란")) {
        return "일평균 유동인구 40-50만명, 20대 여성(패션/뷰티) + 30-40대 남성 직장인, 프리미엄 메뉴 선호, 월세 3.3㎡당 100만원 수준, 체험형 팝업스토어 효과적";
    }
    else if (location.contains("서초")) {
        return "고소득층 밀집, 비즈니스 미팅 장소, 브런치와 파인다이닝 인기, 높은 서비스 기대, 프리미엄 가격대 수용";
    }

    // 홍대 상권 (특화 독립)
    else if (location.contains("홍대") || location.contains("홍익대")) {
        return "전국 최대 클럽/pub 밀집지역, 20대 초반 주 타겟, 심야 매출 집중(21시~새벽2시), 인스타그램 필수 마케팅, 빠른 회전율 중시, 저렴한 가격대와 포토존 필수, 주말 외지인 95% 이상";
    }

    // 홍대/마포권 (젊은층, 트렌디)
    else if (location.contains("마포구") || location.contains("상수") || location.contains("합정") || location.contains("망원")) {
        return "골목상권 특화, 발견하는 재미 중시, 단골가게 문화 발달, 발레파킹 문화 없음, 젊은층 트렌디한 메뉴 선호, SNS 마케팅 필수";
    }

    // 잠실/송파권 (가족형, 대형상권)
    else if (location.contains("송파") || location.contains("잠실")) {
        return "업무시설과 대단위 주거시설 고루 분포, 수요층 안정적, 가족 단위 고객, 아이 친화적 메뉴, 주말 매출 집중, 백화점/몰 연계 고객층";
    }

    // 건대/성수권 (젊은층, 핫플레이스)
    else if (location.contains("광진") || location.contains("성동") || location.contains("건대") || location.contains("성수")) {
        return "대학상권 침체로 성수동으로 유동 증가, 개별 특성 뚜렷한 개인 사업장 위주, 향수/수제화/소품샵 특화, 특별함과 감성 중시, 유흥 이미지 탈피 필요";
    }

    // 이태원/용산권 (외국인, 고급화)
    else if (location.contains("용산") || location.contains("이태원") || location.contains("한남") || location.contains("용리단길")) {
        return "서울 중심부 접근성 우수, 강북-강남 약속장소로 인기, 대기업 직장인 점심 수요, 대단지 아파트 고정 수요층, 외국 콘셉트 특색 있는 점포 증가";
    }

    // 신촌/연대권 (대학가)
    else if (location.contains("서대문") || location.contains("신촌") || location.contains("연대") || location.contains("이화여대")) {
        return "서울시 창업위험도 '위험' 지역 유일, 10대 대학상권 중 최하위 성장률 3.1%, 온라인 쇼핑 확산으로 유동인구 감소, 저렴한 가격대와 대용량 메뉴 필수";
    }

    // 강북/노원권 (가정형, 실용적)
    else if (location.contains("강북") || location.contains("노원")) {
        return "근처 거주민 중심 소비, 상반기 눈에 띄는 매출 성장세, 특화거리 체계적 육성, 가성비 최우선, 동네 단골 관리 중요";
    }

    // 성북/동대문권 (주거+문화예술)
    else if (location.contains("성북") || location.contains("동대문") || location.contains("중랑") || location.contains("성신여대")) {
        return "역사·문화·예술 중심지, 만해 한용운 심우장 등 근현대 문화예술 흔적, 카페/갤러리/로컬상점 매력적 골목상권, 2025년 로컬브랜드 상권 선정, 여성인구 많음";
    }

    // 종로/중구권 (관광+전통)
    else if (location.contains("종로") || location.contains("중구") || location.contains("명동") || location.contains("경동시장")) {
        return "명동 상권 부활, 재래시장 맛집 입소문, 최근 2년 음식업종 매출 평균 증가율 33.3% (서울 최고), 관광객과 직장인 혼재, 전통음식 수요 높음";
    }

    // 영등포/구로권 (직장인, 오피스)
    else if (location.contains("영등포") || location.contains("구로") || location.contains("금천") || location.contains("가산디지털단지")) {
        return "가산디지털단지 상권 활성화 주목, 외식 물가 상승률 상대적 낮음, 직장인 밀집지역, 점심 도시락과 회식 수요, 테이크아웃 선호";
    }

    // 관악/동작권 (대학가+주거)
    else if (location.contains("관악") || location.contains("동작") || location.contains("사당") || location.contains("신림")) {
        return "2025년 샤로수길 로컬브랜드 상권 선정, 대학생과 신혼부부 중심, 저렴한 가격대와 푸짐한 양, 배달 주문 많음, 야식 수요 높음";
    }

    // 양천/강서권 (가정형, 신도시)
    else if (location.contains("양천") || location.contains("강서") || location.contains("목동")) {
        return "저수지 상권으로 떠오름, 목동 상반기 눈에 띄는 매출 성장세, 신도시 가정 고객, 아이 동반 식사, 주차 편의 중시, 브랜드 프랜차이즈 선호";
    }

    // 은평/서대문 외곽권 (주거 밀집)
    else if (location.contains("은평") || location.contains("불광") || location.contains("연신내")) {
        return "전통적 거주지역, 서민과 중산층 혼재, 저가형이나 필수 가전 중심 구매, 객단가 30-50만원 (서울 평균 60만원보다 낮음), 재개발로 상권 변화 중";
    }

    // 창동/도봉권 (저수지 상권)
    else if (location.contains("창동") || location.contains("도봉")) {
        return "2025년 저수지 상권으로 주목, 상반기 눈에 띄는 매출 성장세, 근처 배후 거주민 중심, 한 번 유입된 인구가 그 안에서 소비하는 패턴";
    }

    // 기본값 (일반 주거지역)
    else {
        return "지역 주민 중심, 가성비 중시, 단골 고객 관리 중요, 입소문 마케팅 효과적, 서울시 상권분석 서비스 활용 권장";
    }
}


    private String buildStructuredAnalysisRequest(StoreWeatherInfo weatherInfo,
                                                  boolean hasMenuData,
                                                  boolean hasReviewData) {
        StringBuilder request = new StringBuilder();

        request.append("## 분석 요청사항\n\n");
        request.append("위의 사고과정을 거쳐 다음 3가지 경영 조언을 제시하세요:\n\n");

        if (!hasMenuData) {
            // 신규 매장 또는 데이터 부족
            request.append("### 상황: 신규 매장 또는 메뉴 데이터 부족\n");
            request.append("1. **즉시 실행 날씨 대응** - ").append(weatherInfo.getWeatherSummary())
                    .append(" 날씨에 맞는 오늘 당장 실행할 수 있는 메뉴/서비스 준비\n");
            request.append("2. **초기 고객 유치 전략** - 신규 매장 인지도 확보를 위한 구체적 마케팅 방안\n");
            request.append("3. **리뷰 확보 시스템** - 빠른 시간 내 고객 피드백을 모으는 실용적 방법\n\n");

        } else if (!hasReviewData) {
            // 메뉴는 있지만 리뷰 부족
            request.append("### 상황: 메뉴 데이터 있음, 리뷰 데이터 부족\n");
            request.append("1. **날씨 맞춤 메뉴 홍보** - ").append(weatherInfo.getWeatherSummary())
                    .append(" 날씨에 기존 메뉴를 어떻게 어필할지\n");
            request.append("2. **메뉴 차별화 전략** - 기존 메뉴의 독특함을 부각시키는 방법\n");
            request.append("3. **고객 참여 유도** - 리뷰 작성과 재방문을 늘리는 구체적 이벤트\n\n");

        } else {
            // 충분한 데이터 보유
            request.append("### 상황: 충분한 메뉴 및 리뷰 데이터 보유\n");
            request.append("1. **날씨 기반 메뉴 최적화** - ").append(weatherInfo.getWeatherSummary())
                    .append(" 날씨에 어떤 메뉴를 중점 마케팅할지\n");
            request.append("2. **데이터 기반 운영 개선** - 리뷰 분석 결과를 활용한 구체적 개선 방안\n");
            request.append("3. **매출 극대화 전략** - 기존 강점을 활용한 수익 증대 방법\n\n");
        }

        return request.toString();
    }

    private String buildOutputFormatAndConstraints() {
        return """
                ## 출력 형식 및 제약사항
                
                ### 필수 출력 형식:
                ```
                1. **[구체적 액션 제목]**
                [2-3문장의 구체적 실행방안. 비용, 시간, 방법 명시]
                
                2. **[구체적 액션 제목]**
                [2-3문장의 구체적 실행방안. 예상 효과 포함]
                
                3. **[구체적 액션 제목]**
                [2-3문장의 구체적 실행방안. 측정 방법 포함]
                ```
                
                ### 품질 기준:
                ✅ **즉시 실행**: 오늘 당장 실행 가능해야 함
                ✅ **구체성**: "마케팅 강화" ❌ → "SNS에 메뉴 사진 3장 + 할인 정보 게시" ⭐
                ✅ **측정 가능**: 성과를 숫자로 확인할 수 있어야 함
                ✅ **비용 명시**: 투자 비용이 구체적이어야 함
                ✅ **지역/날씨 반영**: 현재 상황을 반드시 고려
                
                ### 금지사항:
                ❌ 추상적 조언 ("고객 만족도 향상" 등)
                ❌ 장기적 브랜딩 전략
                ❌ 배달 관련 조언 (매장 내 식사 전용)
                ❌ 인사말, 격려 멘트
                ❌ 이모지 사용 (💡, 🔥 등)
                
                ### 검증 체크리스트:
                각 조언이 다음 질문에 YES로 답할 수 있는지 확인:
                - [ ] 오늘 당장 실행할 수 있나?
                - [ ] 구체적인 행동 방안인가?
                - [ ] 비용과 시간이 명시되었나?
                - [ ] 성과를 측정할 수 있나?
                - [ ] 현재 날씨/위치와 관련있나?
                
                위 조건을 모두 만족하는 3개의 조언만 제시하세요.
                """;
    }

    private String buildCurrentBusinessContext(StoreWeatherInfo weatherInfo,
                                               List<MenuAnalysisResult> menuAnalysis,
                                               List<MenuCategory> suggestedCategories) {
        StringBuilder context = new StringBuilder();

        context.append("## 현재 매장 상황 분석\n\n");

        // 기본 정보
        context.append("**매장 기본 정보**:\n");
        context.append("- 위치: ").append(weatherInfo.getLocationSummary()).append("\n");
        context.append("- 현재 날씨: ").append(weatherInfo.getWeatherSummary()).append("\n");
        context.append("- 계절: ").append(weatherInfo.getSeason().getKorean()).append("\n");
        context.append("- 분석 시점: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM월 dd일 HH시"))).append("\n\n");

        // 메뉴 현황
        try {
            Store store = storeRepository.findById(weatherInfo.getStoreId()).orElse(null);
            if (store != null) {
                List<StoreMenu> storeMenus = storeMenuRepository.findByStore(store);
                if (!storeMenus.isEmpty()) {
                    context.append("**보유 메뉴 현황**:\n");
                    storeMenus.stream().limit(10).forEach(menu -> {
                        context.append("- ").append(menu.getMenuName());
                        if (menu.getPrice() != null) {
                            context.append(" (").append(String.format("%,d원", menu.getPrice())).append(")");
                        }
                        if (menu.getCategory() != null) {
                            context.append(" [").append(menu.getCategory()).append("]");
                        }
                        context.append("\n");
                    });
                    if (storeMenus.size() > 10) {
                        context.append("- 외 ").append(storeMenus.size() - 10).append("개 메뉴\n");
                    }
                    context.append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error fetching store menus for enhanced prompt: {}", weatherInfo.getStoreId(), e);
        }

        // 메뉴 성과 분석
        if (menuAnalysis != null && !menuAnalysis.isEmpty()) {
            context.append("**메뉴 성과 분석**:\n");

            // 상위 성과 메뉴
            menuAnalysis.stream()
                    .filter(menu -> menu.getReviewCount() > 0)
                    .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                    .limit(3)
                    .forEach(menu -> {
                        context.append("- 우수: ").append(menu.getMenuName())
                                .append(" (").append(String.format("%.1f★", menu.getAverageRating()))
                                .append(", ").append(menu.getReviewCount()).append("리뷰)");

                        if (menu.getKeyReviewPoints() != null && !menu.getKeyReviewPoints().isEmpty()
                                && !"데이터 없음".equals(menu.getKeyReviewPoints())) {
                            context.append(" - 키워드: ").append(menu.getKeyReviewPoints());
                        }
                        context.append("\n");
                    });

            // 개선 필요 메뉴
            menuAnalysis.stream()
                    .filter(menu -> menu.getReviewCount() > 0 && menu.getAverageRating() < 4.0)
                    .sorted((a, b) -> Double.compare(a.getAverageRating(), b.getAverageRating()))
                    .limit(2)
                    .forEach(menu -> {
                        context.append("- 개선필요: ").append(menu.getMenuName())
                                .append(" (").append(String.format("%.1f★", menu.getAverageRating()))
                                .append(", ").append(menu.getReviewCount()).append("리뷰)\n");
                    });

            context.append("\n");

            // 최근 리뷰 샘플 추가
            addRecentReviewSamples(context, weatherInfo.getStoreId(), 3);
        }

        // 날씨 기반 추천 카테고리
        if (suggestedCategories != null && !suggestedCategories.isEmpty()) {
            context.append("**날씨 맞춤 추천 카테고리**: ");
            context.append(suggestedCategories.stream()
                    .map(MenuCategory::getKorean)
                    .collect(Collectors.joining(", ")));
            context.append("\n\n");
        }

        return context.toString();
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
                    .replaceAll("```[a-zA-Z]*\\n?", "")
                    .replaceAll("```", "")
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
                // 2. *텍스트* -> 텍스트
                .replaceAll("\\*(.*?)\\*", "$1")
                // 3. 줄바꿈을 <br> 태그로 변환
                .replaceAll("\\n", "<br>")
                // 4. 번호 목록 처리 (1. 2. 3. ...)
                .replaceAll("(\\d+\\.)\\s", "<br><strong>$1</strong> ")
                // 5. 불필요한 연속된 <br> 정리
                .replaceAll("(<br>){3,}", "<br><br>")
                // 6. 시작 부분의 <br> 제거
                .replaceAll("^<br>+", "");
    }

    // 최근 리뷰 샘플을 프롬프트에 추가하는 메서드
    private void addRecentReviewSamples(StringBuilder prompt, Long storeId, int maxSamples) {
        try {
            List<CustomerReviewCollect> recentReviews = customerReviewRepository
                    .findByStore_StoreIdOrderByReviewDateDesc(storeId)
                    .stream()
                    .filter(review -> review.getReviewDetails() != null && !review.getReviewDetails().trim().isEmpty())
                    .limit(maxSamples)
                    .collect(Collectors.toList());

            if (!recentReviews.isEmpty()) {
                prompt.append("\n**실제 고객 리뷰 샘플**:\n");
                for (int i = 0; i < recentReviews.size(); i++) {
                    CustomerReviewCollect review = recentReviews.get(i);
                    String menuName = review.getStoreMenu() != null ? review.getStoreMenu().getMenuName() : "메뉴명 불명";
                    String reviewText = review.getReviewDetails().length() > 50 ?
                            review.getReviewDetails().substring(0, 50) + "..." :
                            review.getReviewDetails();

                    prompt.append(String.format("%d. [%s] %.1f★ \"%s\" (%s)\n",
                            i + 1,
                            menuName,
                            review.getScore(),
                            reviewText,
                            review.getReviewDate()
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Error adding review samples to prompt for store: {}", storeId, e);
        }
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
}
