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
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @AuthenticationPrincipal Store store) {

        if (store == null || store.getStoreId() != storeId) {
            log.warn("Unauthorized access attempt to store {} reviews", storeId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (limit > 200) {
            limit = 200;
        }

        try {
            log.info("Store owner fetching all reviews for store: {} with limit: {} offset: {}",
                     storeId, limit, offset);

            List<CustomerReviewCollect> allReviews = customerReviewRepository
                .findByStore_StoreIdOrderByReviewDateDesc(storeId);

            int totalCount = allReviews.size();
            int endIndex = Math.min(offset + limit, totalCount);
            List<CustomerReviewCollect> pagedReviews = allReviews.subList(offset, endIndex);

            log.info("Found {} total reviews for store: {}, returning {} reviews",
                     totalCount, storeId, pagedReviews.size());

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", pagedReviews);
            response.put("totalCount", totalCount);
            response.put("currentPage", offset / limit);
            response.put("hasNext", endIndex < totalCount);

            if (!allReviews.isEmpty()) {
                double averageScore = allReviews.stream()
                    .mapToDouble(CustomerReviewCollect::getScore)
                    .average()
                    .orElse(0.0);
                response.put("averageScore", Math.round(averageScore * 10.0) / 10.0);

                Map<Integer, Long> scoreDistribution = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    final int score = i;
                    long count = allReviews.stream()
                        .filter(r -> (int) r.getScore() == score)
                        .count();
                    scoreDistribution.put(score, count);
                }
                response.put("scoreDistribution", scoreDistribution);

                Map<String, Long> menuReviewCount = allReviews.stream()
                    .filter(r -> r.getStoreMenu() != null && r.getStoreMenu().getMenuName() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getStoreMenu().getMenuName(),
                        java.util.stream.Collectors.counting()
                    ));
                response.put("menuReviewCount", menuReviewCount);
            } else {
                response.put("averageScore", 0.0);
                response.put("scoreDistribution", new HashMap<>());
                response.put("menuReviewCount", new HashMap<>());
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

                long highScoreReviews = allReviews.stream()
                    .filter(r -> r.getScore() >= 4.0)
                    .count();
                double highScoreRatio = (double) highScoreReviews / allReviews.size() * 100;
                summary.put("highScoreRatio", Math.round(highScoreRatio * 10.0) / 10.0);

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