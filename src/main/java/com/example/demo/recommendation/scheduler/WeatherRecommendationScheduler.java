package com.example.demo.recommendation.scheduler;

import com.example.demo.recommendation.dto.MenuRecommendationResponse;
import com.example.demo.recommendation.event.ManualRefreshEvent;
import com.example.demo.recommendation.service.MenuRecommendationService;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.store.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "recommendation.scheduler.enabled", havingValue = "true")
public class WeatherRecommendationScheduler {
    private final MenuRecommendationService recommendationService;
    private final StoreRepository storeRepository;
    private final StoreLocationRepository storeLocationRepository;

    // 매시간 실행 - 위치가 등록된 모든 매장의 추천 갱신
    @Scheduled(cron = "${recommendation.scheduler.cron}")
    @Async
    public void updateAllStoreRecommendations() {
        log.info("Starting scheduled recommendation update for all stores");
        // StoreLocation이 등록된 활성 매장들만 조회
        List<Store> activeStores = storeLocationRepository.findStoresWithLocation();
        log.info("Found {} stores with location data", activeStores.size());
        activeStores.parallelStream().forEach(store -> {
            try {
                updateStoreRecommendation(store);
                Thread.sleep(100); // API 호출 간격 조절
            } catch (Exception e) {
                log.error("Failed to update recommendation for store: {}", store.getStoreId(), e);
            }
        });
        log.info("Completed scheduled recommendation update");
    }

    // 개별 매장 추천 업데이트
    private void updateStoreRecommendation(Store store) {
        try {
            MenuRecommendationResponse recommendation = recommendationService.generateRecommendation(store.getStoreId());
            log.debug("Updated recommendation for store: {} in {}",
                     store.getStoreId(),
                     recommendation.getWeatherInfo().getLocationSummary());
        } catch (Exception e) {
            log.error("Error updating recommendation for store: {}", store.getStoreId(), e);
        }
    }

    // 수동 전체 갱신 트리거
    @EventListener
    public void handleManualRefreshEvent(ManualRefreshEvent event) {
        log.info("Manual refresh triggered for all stores");
        updateAllStoreRecommendations();
    }
}
