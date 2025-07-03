package com.example.demo.recommendation.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuAnalysisResult {
    private Long menuId;
    private String menuName;
    private Double averageRating;
    private Integer reviewCount;
    private Integer salesCount;
    private BigDecimal revenue;
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private String popularityTrend; // RISING, STABLE, DECLINING
    private LocalDateTime analysisDate;
    private String keyReviewPoints; // 주요 리뷰 키워드나 포인트
}
