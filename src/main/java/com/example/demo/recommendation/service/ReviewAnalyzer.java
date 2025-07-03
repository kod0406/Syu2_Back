package com.example.demo.recommendation.service;

import com.example.demo.recommendation.dto.MenuAnalysisResult;
import com.example.demo.store.entity.StoreMenuReview;
import com.example.demo.store.repository.StoreMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalyzer {
    private final StoreMenuRepository storeMenuRepository;

    // 최근 리뷰 분석 (7일 기준)
    public List<MenuAnalysisResult> analyzeRecentReviews(Long storeId) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return analyzeReviews(storeId, since);
    }

    // 특정 기간의 리뷰 분석
    public List<MenuAnalysisResult> analyzeReviews(Long storeId, LocalDateTime since) {
        try {
            // 매장의 메뉴별 리뷰 데이터 조회
            List<StoreMenuReview> reviews = storeMenuRepository.findReviewsByStoreIdAndDate(storeId, since);

            if (reviews.isEmpty()) {
                log.info("No reviews found for store: {} since: {}", storeId, since);
                return new ArrayList<>();
            }

            // 메뉴별로 그룹화하여 분석
            Map<Long, List<StoreMenuReview>> reviewsByMenu = reviews.stream()
                .collect(Collectors.groupingBy(review -> review.getStoreMenu().getMenuId()));

            List<MenuAnalysisResult> results = new ArrayList<>();

            for (Map.Entry<Long, List<StoreMenuReview>> entry : reviewsByMenu.entrySet()) {
                Long menuId = entry.getKey();
                List<StoreMenuReview> menuReviews = entry.getValue();

                MenuAnalysisResult result = analyzeMenuReviews(menuId, menuReviews);
                results.add(result);
            }

            log.info("Analyzed {} menus for store: {}", results.size(), storeId);
            return results;

        } catch (Exception e) {
            log.error("Error analyzing reviews for store: {}", storeId, e);
            return new ArrayList<>();
        }
    }

    // 메뉴별 리뷰 분석
    private MenuAnalysisResult analyzeMenuReviews(Long menuId, List<StoreMenuReview> reviews) {
        if (reviews.isEmpty()) {
            return createEmptyResult(menuId);
        }

        String menuName = reviews.get(0).getStoreMenu().getMenuName();

        // 평균 평점 계산
        Double averageRating = reviews.stream()
            .mapToDouble(StoreMenuReview::getRating)
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

    // 감정 분석 (간단한 키워드 기반)
    private String analyzeSentiment(List<StoreMenuReview> reviews) {
        int positiveCount = 0;
        int negativeCount = 0;

        String[] positiveKeywords = {"맛있", "좋", "추천", "훌륭", "완벽", "최고", "만족"};
        String[] negativeKeywords = {"별로", "아쉽", "실망", "나쁘", "최악", "불만"};

        for (StoreMenuReview review : reviews) {
            String content = review.getReviewContent().toLowerCase();

            for (String keyword : positiveKeywords) {
                if (content.contains(keyword)) {
                    positiveCount++;
                    break;
                }
            }

            for (String keyword : negativeKeywords) {
                if (content.contains(keyword)) {
                    negativeCount++;
                    break;
                }
            }
        }

        if (positiveCount > negativeCount) {
            return "POSITIVE";
        } else if (negativeCount > positiveCount) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }

    // 인기도 트렌드 분석
    private String analyzePopularityTrend(List<StoreMenuReview> reviews) {
        if (reviews.size() < 2) {
            return "STABLE";
        }

        // 최근 3일과 이전 기간 비교
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        long recentCount = reviews.stream()
            .filter(review -> review.getReviewDate().isAfter(threeDaysAgo))
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
    private String extractKeyPoints(List<StoreMenuReview> reviews) {
        Map<String, Integer> keywordCount = reviews.stream()
            .flatMap(review ->
                List.of(review.getReviewContent().split("\\s+")).stream()
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
}
