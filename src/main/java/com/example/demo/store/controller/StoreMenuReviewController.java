package com.example.demo.store.controller;

import com.example.demo.customer.entity.CustomerReviewCollect;
import com.example.demo.customer.repository.CustomerReviewCollectRepository;
import com.example.demo.store.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores/{storeId}/reviews")
@Slf4j
@RequiredArgsConstructor
public class StoreMenuReviewController {

    private final CustomerReviewCollectRepository customerReviewRepository;

    @GetMapping("/menus/{menuId}")
    public ResponseEntity<Map<String, Object>> getMenuReviews(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            log.warn("Unauthorized access attempt to store {} reviews", storeId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            log.info("Store owner fetching reviews for store: {} menu: {}", storeId, menuId);

            List<CustomerReviewCollect> reviews = customerReviewRepository
                .findByStoreIdAndMenuId(storeId, menuId);

            log.info("Found {} reviews for store: {} menu: {}", reviews.size(), storeId, menuId);

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews);
            response.put("totalCount", reviews.size());

            if (!reviews.isEmpty()) {
                double averageScore = reviews.stream()
                    .mapToDouble(CustomerReviewCollect::getScore)
                    .average()
                    .orElse(0.0);
                response.put("averageScore", Math.round(averageScore * 10.0) / 10.0);

                Map<Integer, Long> scoreDistribution = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    final int score = i;
                    long count = reviews.stream()
                        .filter(r -> (int) r.getScore() == score)
                        .count();
                    scoreDistribution.put(score, count);
                }
                response.put("scoreDistribution", scoreDistribution);
            } else {
                response.put("averageScore", 0.0);
                response.put("scoreDistribution", new HashMap<>());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching reviews for store: {} menu: {}", storeId, menuId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date_desc") String sort,
            @RequestParam(required = false) String menu,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            log.warn("Unauthorized access attempt to store {} reviews", storeId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (size > 200) {
            size = 200;
        }

        try {
            log.info("Store owner fetching all reviews for store: {} with page: {} size: {} sort: {} menu: {}",
                     storeId, page, size, sort, menu);

            List<CustomerReviewCollect> allReviews = customerReviewRepository
                .findByStore_StoreIdOrderByReviewDateDesc(storeId);

            // 메뉴 필터링
            List<CustomerReviewCollect> filteredReviews = allReviews;
            if (menu != null && !menu.trim().isEmpty()) {
                Long menuId = Long.parseLong(menu);
                filteredReviews = allReviews.stream()
                    .filter(r -> r.getStoreMenu() != null && menuId.equals(r.getStoreMenu().getMenuId()))
                    .collect(Collectors.toList());
            }

            // 정렬 적용
            switch (sort) {
                case "date_asc":
                    filteredReviews.sort((a, b) -> a.getReviewDate().compareTo(b.getReviewDate()));
                    break;
                case "date_desc":
                    filteredReviews.sort((a, b) -> b.getReviewDate().compareTo(a.getReviewDate()));
                    break;
                case "score_asc":
                    filteredReviews.sort((a, b) -> Double.compare(a.getScore(), b.getScore()));
                    break;
                case "score_desc":
                    filteredReviews.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
                    break;
                default:
                    // 기본값: date_desc
                    filteredReviews.sort((a, b) -> b.getReviewDate().compareTo(a.getReviewDate()));
            }

            // 페이지네이션 (page는 1부터 시작)
            int totalCount = filteredReviews.size();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            int offset = (page - 1) * size;
            int endIndex = Math.min(offset + size, totalCount);

            List<CustomerReviewCollect> pagedReviews = offset < totalCount ?
                filteredReviews.subList(offset, endIndex) : List.of();

            log.info("Found {} total reviews for store: {}, returning {} reviews (page {}/{})",
                     totalCount, storeId, pagedReviews.size(), page, totalPages);

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", pagedReviews);
            response.put("totalCount", totalCount);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("hasNext", page < totalPages);
            response.put("hasPrevious", page > 1);

            if (!filteredReviews.isEmpty()) {
                double averageScore = filteredReviews.stream()
                    .mapToDouble(CustomerReviewCollect::getScore)
                    .average()
                    .orElse(0.0);
                response.put("averageScore", Math.round(averageScore * 10.0) / 10.0);

                Map<Integer, Long> scoreDistribution = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    final int score = i;
                    long count = filteredReviews.stream()
                        .filter(r -> (int) r.getScore() == score)
                        .count();
                    scoreDistribution.put(score, count);
                }
                response.put("scoreDistribution", scoreDistribution);

                // filteredReviews를 사용하여 메뉴별 리뷰 수 계산 (수정됨)
                Map<String, Long> menuReviewCount = filteredReviews.stream()
                    .filter(r -> r.getStoreMenu() != null && r.getStoreMenu().getMenuName() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getStoreMenu().getMenuName(),
                        java.util.stream.Collectors.counting()
                    ));

                // 프론트엔드에서 기대하는 형태로 변환
                final List<CustomerReviewCollect> finalFilteredReviews = filteredReviews; // final 변수로 생성
                List<Map<String, Object>> menuReviewCounts = menuReviewCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> menuCount = new HashMap<>();
                        menuCount.put("menuName", entry.getKey());
                        menuCount.put("reviewCount", entry.getValue());
                        // menuId 추가 (필터링에 필요)
                        Long menuId = finalFilteredReviews.stream()
                            .filter(r -> r.getStoreMenu() != null &&
                                       entry.getKey().equals(r.getStoreMenu().getMenuName()))
                            .findFirst()
                            .map(r -> r.getStoreMenu().getMenuId())
                            .orElse(0L);
                        menuCount.put("menuId", menuId);
                        return menuCount;
                    })
                    .sorted((a, b) -> Long.compare((Long)b.get("reviewCount"), (Long)a.get("reviewCount")))
                    .collect(Collectors.toList());

                response.put("menuReviewCounts", menuReviewCounts);
                response.put("menuReviewCount", menuReviewCount);
            } else {
                response.put("averageScore", 0.0);
                response.put("scoreDistribution", new HashMap<>());
                response.put("menuReviewCount", new HashMap<>());
                response.put("menuReviewCounts", List.of());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching all reviews for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getReviewSummary(
            @PathVariable Long storeId,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            log.warn("Unauthorized access attempt to store {} review summary", storeId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            log.info("Store owner fetching review summary for store: {}", storeId);

            List<CustomerReviewCollect> allReviews = customerReviewRepository
                .findByStore_StoreIdOrderByReviewDateDesc(storeId);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalReviews", allReviews.size());

            if (!allReviews.isEmpty()) {
                double averageScore = allReviews.stream()
                    .mapToDouble(CustomerReviewCollect::getScore)
                    .average()
                    .orElse(0.0);
                summary.put("averageScore", Math.round(averageScore * 10.0) / 10.0);

                long recentReviews = allReviews.stream()
                    .filter(r -> r.getReviewDate().isAfter(
                        java.time.LocalDate.now().minusDays(30)))
                    .count();
                summary.put("recentReviews", recentReviews);
                summary.put("recentThirtyDaysCount", recentReviews); // 프론트엔드 호환성

                long highScoreReviews = allReviews.stream()
                    .filter(r -> r.getScore() >= 4.0)
                    .count();
                double highScoreRatio = (double) highScoreReviews / allReviews.size() * 100;
                summary.put("highScoreRatio", Math.round(highScoreRatio * 10.0) / 10.0);
                summary.put("highRatingPercentage", Math.round(highScoreRatio * 10.0) / 10.0); // 프론트엔드 호환성

                // 메뉴별 리뷰 수 추가
                Map<String, Long> menuReviewCount = allReviews.stream()
                    .filter(r -> r.getStoreMenu() != null && r.getStoreMenu().getMenuName() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getStoreMenu().getMenuName(),
                        java.util.stream.Collectors.counting()
                    ));

                // 프론트엔드에서 기대하는 형태로 변환
                List<Map<String, Object>> menuReviewCounts = menuReviewCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> menuCount = new HashMap<>();
                        menuCount.put("menuName", entry.getKey());
                        menuCount.put("reviewCount", entry.getValue());
                        // menuId 추가 (필터링에 필요)
                        Long menuId = allReviews.stream()
                            .filter(r -> r.getStoreMenu() != null &&
                                       entry.getKey().equals(r.getStoreMenu().getMenuName()))
                            .findFirst()
                            .map(r -> r.getStoreMenu().getMenuId())
                            .orElse(0L);
                        menuCount.put("menuId", menuId);
                        return menuCount;
                    })
                    .sorted((a, b) -> Long.compare((Long)b.get("reviewCount"), (Long)a.get("reviewCount")))
                    .collect(Collectors.toList());

                summary.put("menuReviewCounts", menuReviewCounts); // 배열 형태로 변경
                summary.put("menuReviewCount", menuReviewCount); // 기존 호환성 유지

            } else {
                summary.put("averageScore", 0.0);
                summary.put("recentReviews", 0);
                summary.put("highScoreRatio", 0.0);
            }

            log.info("Review summary for store {}: {}", storeId, summary);
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("Error fetching review summary for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
