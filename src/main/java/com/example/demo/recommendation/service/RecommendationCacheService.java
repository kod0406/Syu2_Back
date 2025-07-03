package com.example.demo.recommendation.service;

import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.store.entity.MenuRecommendationCache;
import com.example.demo.store.entity.Store;
import com.example.demo.recommendation.repository.MenuRecommendationCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private final ObjectMapper objectMapper;

    @Value("${recommendation.cache.duration}")
    private long cacheDuration;

    // 캐시된 추천 조회
    public Optional<MenuRecommendationResponse> getCachedRecommendation(Long storeId) {
        // 1. Redis 캐시 확인 (빠른 조회)
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
        // 2. DB 캐시 확인 (Redis 실패 시)
        return cacheRepository.findValidCacheByStoreId(storeId, LocalDateTime.now())
            .map(this::convertToResponse);
    }

    // 추천 결과 캐시 저장
    public void saveRecommendation(MenuRecommendationResponse response) {
        String cacheKey = generateCacheKey(response.getStoreId());
        LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(cacheDuration);
        try {
            // 1. Redis 저장
            String jsonValue = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, Duration.ofSeconds(cacheDuration));
            // 2. DB 저장 (MenuRecommendationCache 엔티티 활용)
            MenuRecommendationCache cacheEntity = MenuRecommendationCache.builder()
                .store(response.getWeatherInfo().getStore())
                .gptRecommendation(jsonValue)
                .weatherCondition(response.getWeatherInfo().getWeatherType().name())
                .season(response.getWeatherInfo().getSeason().name())
                .expiredAt(expiredAt)
                .build();
            cacheRepository.save(cacheEntity);
            log.info("Cache saved for store: {}, expires at: {}", response.getStoreId(), expiredAt);
        } catch (Exception e) {
            log.error("Cache save error for store: {}", response.getStoreId(), e);
        }
    }

    // 추천 히스토리 조회 (MenuRecommendationService에서 사용)
    public List<MenuRecommendationCache> getRecommendationHistory(Store store, LocalDateTime since) {
        try {
            return cacheRepository.findByStoreAndCreatedAtAfterOrderByCreatedAtDesc(store, since);
        } catch (Exception e) {
            log.error("Error getting recommendation history for store: {}", store.getStoreId(), e);
            return new ArrayList<>();
        }
    }

    // 캐시 키 생성 (storeId 기반)
    private String generateCacheKey(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("menu_recommendation:%d:%d:%d", storeId, now.getDayOfYear(), now.getHour());
    }

    // DB 캐시를 Response로 변환
    private MenuRecommendationResponse convertToResponse(MenuRecommendationCache cache) {
        try {
            return objectMapper.readValue(cache.getGptRecommendation(), MenuRecommendationResponse.class);
        } catch (Exception e) {
            log.error("DB cache parsing error for cache id: {}", cache.getId(), e);
            return null;
        }
    }

    // 만료된 캐시 정리 (스케줄러에서 호출)
    @Scheduled(cron = "0 0 */6 * * ?") // 6시간마다
    public void cleanExpiredCache() {
        // Redis는 TTL로 자동 삭제
        // DB 캐시만 수동 정리
        cacheRepository.deleteExpiredCache(LocalDateTime.now());
        log.info("Expired cache cleaned");
    }
}
