package com.example.demo.external.gemini.service;

import com.example.demo.external.gemini.dto.GeminiRequest;
import com.example.demo.external.gemini.dto.GeminiResponse;
import com.example.demo.external.gemini.dto.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiApiService {
    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;


    // AI 추천 생성 서비스

    // 메뉴 추천 생성 (MenuRecommendationService에서 사용)
    public Mono<String> generateMenuRecommendation(String prompt) {
        try {
            GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                    Content.builder()
                        .parts(List.of(Content.Part.builder().text(prompt).build()))
                        .build()
                ))
                .build();

            String fullUrl = apiUrl + "?key=" + apiKey;
            log.info("Gemini API 요청 URL: {}", fullUrl);
            log.info("Gemini API 요청 파라미터: prompt={}", prompt);

            // 재시도 및 타임아웃 적용
            return webClient.post()
                .uri(fullUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    }
                    return "AI 응답을 처리할 수 없습니다.";
                })
                .doOnError(error -> log.error("Gemini API error: ", error))
                .onErrorReturn("AI 서비스 일시 장애로 기본 추천을 제공합니다.");
        } catch (Exception e) {
            log.error("Error creating Gemini request", e);
            return Mono.just("AI 서비스 일시 장애로 기본 추천을 제공합니다.");
        }
    }

    // 일반적인 텍스트 생성
    public Mono<String> generateText(String prompt) {
        return generateMenuRecommendation(prompt);
    }
}
