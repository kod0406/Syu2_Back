package com.example.demo.recommendation.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RecommendationHistoryResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String aiAdvice;  // 정규식 처리된 HTML 형태
    private String weatherCondition;
    private String season;
    private LocalDateTime createdAt;

    // 추가 정보 (필요한 경우)
    private String rawAiAdvice;  // 원본 텍스트 (옵션)
}
