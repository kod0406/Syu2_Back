package com.example.demo.recommendation.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAdviceResult {
    private String advice;
    private String menuRecommendation;
    private String marketingStrategy;
    private String improvementSuggestion;
    private List<String> keyPoints;
    private Double confidenceScore;
    private String aiModel; // "gemini", "gpt" etc
    private LocalDateTime generatedAt;
    private String prompt;
    private boolean isError;
    private String errorMessage;
}
