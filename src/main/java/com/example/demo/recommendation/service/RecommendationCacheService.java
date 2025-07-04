package com.example.demo.recommendation.service;

import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.dto.RecommendationHistoryResponse;
import com.example.demo.store.entity.MenuRecommendationCache;
import com.example.demo.store.entity.MenuRecommendationHistory;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.recommendation.repository.MenuRecommendationCacheRepository;
import com.example.demo.recommendation.repository.MenuRecommendationHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final MenuRecommendationCacheRepository cacheRepository;
    private final MenuRecommendationHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final StoreRepository storeRepository;

    @Value("${recommendation.cache.duration}")
    private long cacheDuration;

    public Optional<MenuRecommendationResponse> getCachedRecommendation(Long storeId) {
        String cacheKey = generateCacheKey(storeId);
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return Optional.of(objectMapper.readValue(cachedJson, MenuRecommendationResponse.class));
            } catch (Exception e) {
                log.error("Redis cache parsing error for store: {}", storeId, e);
                redisTemplate.delete(cacheKey);
            }
        }
        return cacheRepository.findValidCacheByStoreId(storeId, LocalDateTime.now())
            .map(this::convertToResponse);
    }

    public void saveRecommendation(MenuRecommendationResponse response) {
        String cacheKey = generateCacheKey(response.getStoreId());
        LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(cacheDuration);
        try {
            String jsonValue = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, Duration.ofSeconds(cacheDuration));

            // Store 엔티티를 직접 조회
            Store store = storeRepository.findById(response.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + response.getStoreId()));

            String weatherCondition = response.getWeatherInfo().getWeatherType().name();
            String season = response.getWeatherInfo().getSeason().name();

            Optional<MenuRecommendationCache> existingCache =
                cacheRepository.findByStoreAndWeatherConditionAndSeason(store, weatherCondition, season);

            if (existingCache.isPresent()) {
                MenuRecommendationCache cache = existingCache.get();
                cacheRepository.updateCacheContent(cache.getId(), jsonValue, expiredAt);
                log.info("Cache updated for store: {}, condition: {}, season: {}",
                    response.getStoreId(), weatherCondition, season);
            } else {
                try {
                    MenuRecommendationCache cacheEntity = MenuRecommendationCache.builder()
                        .store(store)
                        .gptRecommendation(jsonValue)
                        .weatherCondition(weatherCondition)
                        .season(season)
                        .expiredAt(expiredAt)
                        .build();
                    cacheRepository.save(cacheEntity);
                    log.info("New cache created for store: {}, condition: {}, season: {}",
                        response.getStoreId(), weatherCondition, season);
                } catch (DataIntegrityViolationException e) {
                    log.warn("Concurrent cache creation detected, updating existing cache for store: {}", response.getStoreId());
                    existingCache = cacheRepository.findByStoreAndWeatherConditionAndSeason(store, weatherCondition, season);
                    if (existingCache.isPresent()) {
                        cacheRepository.updateCacheContent(existingCache.get().getId(), jsonValue, expiredAt);
                    }
                }
            }

            MenuRecommendationHistory historyEntity = MenuRecommendationHistory.builder()
                .store(store)
                .gptRecommendation(jsonValue)
                .weatherCondition(weatherCondition)
                .season(season)
                .build();
            historyRepository.save(historyEntity);

            log.info("Cache and history saved for store: {}, expires at: {}", response.getStoreId(), expiredAt);
        } catch (Exception e) {
            log.error("Cache/history save error for store: {}", response.getStoreId(), e);
        }
    }

    public List<RecommendationHistoryResponse> getRecommendationHistory(Store store, LocalDateTime since) {
        try {
            List<MenuRecommendationHistory> histories = historyRepository.findByStoreAndCreatedAtAfterOrderByCreatedAtDesc(store, since);

            return histories.stream()
                .map(this::convertToHistoryResponse)
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recommendation history for store: {}", store.getStoreId(), e);
            return new ArrayList<>();
        }
    }

    private RecommendationHistoryResponse convertToHistoryResponse(MenuRecommendationHistory history) {
        try {
            String gptResponse = history.getGptRecommendation();
            String aiAdvice = extractAiAdviceFromJson(gptResponse);
            String formattedAdvice = null;

            if (aiAdvice != null) {
                formattedAdvice = formatAiAdvice(aiAdvice);
            }

            return RecommendationHistoryResponse.builder()
                .id(history.getId())
                .storeId(history.getStore().getStoreId())
                .storeName(history.getStore().getStoreName())
                .aiAdvice(formattedAdvice != null ? formattedAdvice : "AI 조언을 처리할 수 없습니다.")
                .rawAiAdvice(aiAdvice)
                .weatherCondition(history.getWeatherCondition())
                .season(history.getSeason())
                .createdAt(history.getCreatedAt())
                .build();

        } catch (Exception e) {
            log.warn("Error converting history to response for id: {}", history.getId(), e);

            return RecommendationHistoryResponse.builder()
                .id(history.getId())
                .storeId(history.getStore().getStoreId())
                .storeName(history.getStore().getStoreName())
                .aiAdvice("히스토리 데이터를 처리하는 중 문제가 발생했습니다.")
                .weatherCondition(history.getWeatherCondition())
                .season(history.getSeason())
                .createdAt(history.getCreatedAt())
                .build();
        }
    }

    private String extractAiAdviceFromJson(String jsonString) {
        try {
            String pattern = "\"aiAdvice\"\\s*:\\s*\"(.*?)\"(?=\\s*[,}])";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = regex.matcher(jsonString);

            if (matcher.find()) {
                return matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
            }
        } catch (Exception e) {
            log.warn("Could not extract aiAdvice from JSON: {}", e.getMessage());
        }
        return null;
    }

    private String formatAiAdvice(String aiAdvice) {
        if (aiAdvice == null || aiAdvice.isEmpty()) {
            return "AI 조언이 제공되지 않았습니다.";
        }
        return aiAdvice.trim().replaceAll("\\s+", " ");
    }

    private String generateCacheKey(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("menu_recommendation:%d:%d:%d", storeId, now.getDayOfYear(), now.getHour());
    }

    private MenuRecommendationResponse convertToResponse(MenuRecommendationCache cache) {
        try {
            return objectMapper.readValue(cache.getGptRecommendation(), MenuRecommendationResponse.class);
        } catch (Exception e) {
            log.error("DB cache parsing error for cache id: {}", cache.getId(), e);
            return null;
        }
    }

    @Scheduled(cron = "0 0 */6 * * ?")
    public void cleanExpiredCache() {
        cacheRepository.deleteExpiredCache(LocalDateTime.now());
        log.info("Expired cache cleaned");
    }
}