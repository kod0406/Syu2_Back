package com.example.demo.recommendation.service;

import com.example.demo.recommendation.config.SentimentAnalysisConfig;
import com.example.demo.recommendation.dto.MenuAnalysisResult;
import com.example.demo.customer.entity.CustomerReviewCollect;
import com.example.demo.customer.repository.CustomerReviewCollectRepository;
import com.example.demo.store.repository.StoreMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalyzer {
    private final CustomerReviewCollectRepository customerReviewRepository;
    private final StoreMenuRepository storeMenuRepository;
    private final SentimentAnalysisConfig sentimentConfig;

    // 최근 리뷰 분석 (7일 기준, 없으면 단계적으로 확장)
    public List<MenuAnalysisResult> analyzeRecentReviews(Long storeId) {
        // 먼저 기존 리뷰 조회 API 로직을 활용
        List<CustomerReviewCollect> allStoreReviews = customerReviewRepository
            .findByStore_StoreIdOrderByReviewDateDesc(storeId);

        log.info("=== 리뷰 데이터 조회 결과 (Store ID: {}) ===", storeId);
        log.info("전체 리뷰 개수: {}", allStoreReviews.size());

        if (!allStoreReviews.isEmpty()) {
            log.info("최신 리뷰 3개:");
            allStoreReviews.stream().limit(3).forEach(review -> {
                log.info("- 메뉴: {}, 평점: {}, 날짜: {}, 내용: {}",
                    review.getStoreMenu() != null ? review.getStoreMenu().getMenuName() : "알수없음",
                    review.getScore(),
                    review.getReviewDate(),
                    review.getReviewDetails() != null ?
                        (review.getReviewDetails().length() > 30 ?
                            review.getReviewDetails().substring(0, 30) + "..." :
                            review.getReviewDetails()) : "내용없음"
                );
            });
        }
        log.info("=== 리뷰 데이터 조회 끝 ===");

        if (allStoreReviews.isEmpty()) {
            log.info("No reviews found for store: {}, returning no-review result", storeId);
            return getMenusWithoutReviews(storeId);
        }

        // 단계적으로 기간을 늘려가며 리뷰 조회
        int[] dayPeriods = {7, 14, 30, 90, 365}; // 7일 -> 2주 -> 1달 -> 3달 -> 1년

        for (int days : dayPeriods) {
            LocalDate since = LocalDate.now().minusDays(days);

            // 전체 리뷰에서 날짜 필터링
            List<CustomerReviewCollect> filteredReviews = allStoreReviews.stream()
                .filter(review -> review.getReviewDate() != null &&
                         review.getReviewDate().isAfter(since.minusDays(1))) // 포함하기 위해 -1일
                .collect(Collectors.toList());

            List<MenuAnalysisResult> results = analyzeReviewList(storeId, filteredReviews);

            // 리뷰가 있으면 반환
            if (!results.isEmpty() && !isNoReviewResult(results)) {
                log.info("Found {} reviews for store: {} within {} days", filteredReviews.size(), storeId, days);
                return results;
            }

            log.info("No reviews found for store: {} within {} days, trying longer period", storeId, days);
        }

        // 모든 기간에서 리뷰를 찾지 못한 경우 전체 리뷰로 분석
        log.info("Using all available reviews for store: {}", storeId);
        return analyzeReviewList(storeId, allStoreReviews);
    }

    // 리뷰 리스트를 분석하는 메서드
    private List<MenuAnalysisResult> analyzeReviewList(Long storeId, List<CustomerReviewCollect> reviews) {
        try {
            if (reviews.isEmpty()) {
                return getMenusWithoutReviews(storeId);
            }

            // 메뉴별로 그룹화하여 분석
            Map<Long, List<CustomerReviewCollect>> reviewsByMenu = reviews.stream()
                .filter(review -> review.getStoreMenu() != null)
                .collect(Collectors.groupingBy(review -> review.getStoreMenu().getMenuId()));

            List<MenuAnalysisResult> results = new ArrayList<>();

            for (Map.Entry<Long, List<CustomerReviewCollect>> entry : reviewsByMenu.entrySet()) {
                Long menuId = entry.getKey();
                List<CustomerReviewCollect> menuReviews = entry.getValue();

                MenuAnalysisResult result = analyzeMenuReviews(menuId, menuReviews);
                if (result != null) {
                    results.add(result);
                }
            }

            log.info("Analyzed {} menus for store: {}", results.size(), storeId);
            return results;

        } catch (Exception e) {
            log.error("Error analyzing review list for store: {}", storeId, e);
            return getMenusWithoutReviews(storeId);
        }
    }

    // 리뷰가 없는 메뉴들을 기본 분석 결과로 반환
    private List<MenuAnalysisResult> getMenusWithoutReviews(Long storeId) {
        try {
            // 매장의 메뉴 개수 확인
            long menuCount = storeMenuRepository.countByStoreStoreId(storeId);
            log.info("Store {} has {} menus", storeId, menuCount);

            if (menuCount > 0) {
                // 메뉴는 있지만 리뷰가 없는 경우
                return List.of(createNoReviewResult(storeId));
            }

            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting menus for store: {}", storeId, e);
            return new ArrayList<>();
        }
    }

    // 리뷰가 없는 매장용 기본 결과 생성
    private MenuAnalysisResult createNoReviewResult(Long storeId) {
        return MenuAnalysisResult.builder()
            .menuId(0L)
            .menuName("메뉴 정보 로딩 중")
            .averageRating(0.0)
            .reviewCount(0)
            .salesCount(0)
            .revenue(BigDecimal.ZERO)
            .sentiment("데이터 부족")
            .popularityTrend("분석 불가")
            .analysisDate(LocalDateTime.now())
            .keyReviewPoints("리뷰 데이터가 충분하지 않습니다.")
            .build();
    }

    // 메뉴별 리뷰 분석
    private MenuAnalysisResult analyzeMenuReviews(Long menuId, List<CustomerReviewCollect> reviews) {
        if (reviews.isEmpty()) {
            return createEmptyResult(menuId);
        }

        String menuName = reviews.get(0).getStoreMenu().getMenuName();

        // 평균 평점 계산
        Double averageRating = reviews.stream()
            .mapToDouble(CustomerReviewCollect::getScore)
            .average()
            .orElse(0.0);

        // 리뷰 수
        Integer reviewCount = reviews.size();

        // 감정 분석
        String sentiment = analyzeSentiment(reviews);

        // 인기도 트렌드 분석
        String popularityTrend = analyzePopularityTrend(reviews);

        // 주요 리뷰 포인트 추출
        String keyReviewPoints = extractKeyPoints(reviews);

        return MenuAnalysisResult.builder()
            .menuId(menuId)
            .menuName(menuName)
            .averageRating(averageRating)
            .reviewCount(reviewCount)
            .salesCount(0) // 판매량은 별도 데이터 필요
            .revenue(BigDecimal.ZERO) // 매출은 별도 데이터 필요
            .sentiment(sentiment)
            .popularityTrend(popularityTrend)
            .analysisDate(LocalDateTime.now())
            .keyReviewPoints(keyReviewPoints)
            .build();
    }

    // 감정 분석
    private String analyzeSentiment(List<CustomerReviewCollect> reviews) {
    double totalScore = 0;
    int reviewCount = 0;

    for (CustomerReviewCollect review : reviews) {
        if (review.getReviewDetails() == null) continue;

        String content = review.getReviewDetails().toLowerCase().trim();
        double reviewScore = analyzeReviewSentiment(content);
        totalScore += reviewScore;
        reviewCount++;
    }

    if (reviewCount == 0) return "NEUTRAL";

    double averageScore = totalScore / reviewCount;

    if (averageScore > sentimentConfig.getThreshold().getPositive()) {
        return "POSITIVE";
    } else if (averageScore < sentimentConfig.getThreshold().getNegative()) {
        return "NEGATIVE";
    } else {
        return "NEUTRAL";
    }
}

private double analyzeReviewSentiment(String content) {
    double score = 0;

    // 특별 패턴 먼저 체크 (우선순위)
    score += checkSpecialPatterns(content);

    // 강한 표현 체크 (가중치 2배)
    score += analyzeKeywords(content, sentimentConfig.getPositive().getStrong(), sentimentConfig.getWeight().getStrong());
    score -= analyzeKeywords(content, sentimentConfig.getNegative().getStrong(), sentimentConfig.getWeight().getStrong());

    // 일반 키워드 체크
    score += analyzeKeywords(content, sentimentConfig.getPositive().getBasic(), sentimentConfig.getWeight().getBasic());
    score -= analyzeKeywords(content, sentimentConfig.getNegative().getBasic(), sentimentConfig.getWeight().getBasic());

    // 서비스 관련 키워드
    score += analyzeKeywords(content, sentimentConfig.getPositive().getService(), sentimentConfig.getWeight().getBasic());

    // 맛 관련 부정 키워드
    score -= analyzeKeywords(content, sentimentConfig.getNegative().getTaste(), sentimentConfig.getWeight().getBasic());

    return score;
}

private double checkSpecialPatterns(String content) {
    double score = 0;

    // 재방문 의사 긍정
    for (String pattern : sentimentConfig.getPositive().getRevisit()) {
        if (content.contains(pattern)) {
            score += sentimentConfig.getWeight().getSpecial();
            break; // 한 번만 적용
        }
    }

    // 재방문 거부 부정
    for (String pattern : sentimentConfig.getNegative().getRevisit()) {
        if (content.contains(pattern)) {
            score -= sentimentConfig.getWeight().getSpecial();
            break; // 한 번만 적용
        }
    }

    return score;
}

private double analyzeKeywords(String content, List<String> keywords, double weight) {
    double score = 0;

    for (String keyword : keywords) {
        if (content.contains(keyword)) {
            if (hasNegationBefore(content, keyword)) {
                // 부정어가 앞에 있으면 반전
                if (weight == sentimentConfig.getWeight().getStrong()) {
                    score -= weight; // 강한 표현은 그대로 반전
                } else {
                    score -= weight; // 일반 표현도 반전
                }
            } else {
                score += weight;
            }
        }
    }
    return score;
}

// 부정어가 키워드 앞에 있는지 체크
private boolean hasNegationBefore(String content, String keyword) {
    int keywordIndex = content.indexOf(keyword);
    if (keywordIndex == -1) return false;

    String beforeKeyword = content.substring(0, keywordIndex);
    int searchRange = sentimentConfig.getNegation().getSearch().getRange();

    for (String negation : sentimentConfig.getNegation().getPatterns()) {
        // 키워드 바로 앞이나 설정된 범위 내에 부정어가 있는지 체크
        if (beforeKeyword.endsWith(negation) ||
            beforeKeyword.contains(negation + " ") ||
            (beforeKeyword.length() > searchRange &&
             beforeKeyword.substring(beforeKeyword.length() - searchRange).contains(negation))) {
            return true;
        }
    }
    return false;
}

// 부정어가 키워드 앞에 있는지 체크
private boolean hasNegationBefore(String content, String keyword, String[] negationPatterns) {
    int keywordIndex = content.indexOf(keyword);
    if (keywordIndex == -1) return false;

    String beforeKeyword = content.substring(0, keywordIndex);

    for (String negation : negationPatterns) {
        // 키워드 바로 앞이나 5글자 이내에 부정어가 있는지 체크
        if (beforeKeyword.endsWith(negation) ||
            beforeKeyword.contains(negation + " ") ||
            (beforeKeyword.length() > 5 &&
             beforeKeyword.substring(beforeKeyword.length() - 5).contains(negation))) {
            return true;
        }
    }
    return false;
}


    // 인기도 트렌드 분석
    private String analyzePopularityTrend(List<CustomerReviewCollect> reviews) {
        if (reviews.size() < 2) {
            return "STABLE";
        }

        // LocalDate 기반 날짜 비교로 수정 (JPA 컨버터가 처리)
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

        long recentCount = reviews.stream()
            .filter(review -> review.getReviewDate() != null && review.getReviewDate().isAfter(threeDaysAgo))
            .count();

        long previousCount = reviews.size() - recentCount;

        if (recentCount > previousCount * 1.5) {
            return "RISING";
        } else if (recentCount < previousCount * 0.5) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    // 주요 리뷰 포인트 추출
    private String extractKeyPoints(List<CustomerReviewCollect> reviews) {
        Map<String, Integer> keywordCount = reviews.stream()
            .filter(review -> review.getReviewDetails() != null)
            .flatMap(review ->
                List.of(review.getReviewDetails().split("\\s+")).stream()
                    .filter(word -> word.length() > 1)
                    .map(String::toLowerCase)
            )
            .collect(Collectors.groupingBy(
                word -> word,
                Collectors.summingInt(word -> 1)
            ));

        return keywordCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(", "));
    }

    // 빈 결과 생성
    private MenuAnalysisResult createEmptyResult(Long menuId) {
        return MenuAnalysisResult.builder()
            .menuId(menuId)
            .menuName("Unknown")
            .averageRating(0.0)
            .reviewCount(0)
            .salesCount(0)
            .revenue(BigDecimal.ZERO)
            .sentiment("NEUTRAL")
            .popularityTrend("STABLE")
            .analysisDate(LocalDateTime.now())
            .keyReviewPoints("데이터 없음")
            .build();
    }

    // 리뷰 없음 결과인지 확인하는 헬퍼 메서드
    private boolean isNoReviewResult(List<MenuAnalysisResult> results) {
        return results.size() == 1 &&
               results.get(0).getMenuId() == 0L &&
               "메뉴 정보 로딩 중".equals(results.get(0).getMenuName());
    }
}
