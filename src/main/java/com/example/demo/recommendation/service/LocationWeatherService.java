package com.example.demo.recommendation.service;

import com.example.demo.external.weather.service.WeatherApiService;
import com.example.demo.external.weather.dto.WeatherResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreLocation;
import com.example.demo.store.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationWeatherService {
    private final WeatherApiService weatherApiService;
    private final StoreLocationRepository storeLocationRepository;

    // StoreLocation을 이용한 날씨 조회
    public StoreWeatherInfo getStoreWeatherInfo(Long storeId) {
        Store store = findStore(storeId);
        StoreLocation location = findStoreLocation(store);

        // 위도/경도로 날씨 조회
        WeatherResponse weather = weatherApiService.getCurrentWeather(
            location.getLatitude(),
            location.getLongitude()
        ).block();

        return StoreWeatherInfo.builder()
            .store(store)
            .location(location)
            .weather(weather)
            .weatherType(weatherApiService.determineWeatherType(weather))
            .season(getCurrentSeason())
            .build();
    }

    // StoreLocation이 없는 경우 처리
    private StoreLocation findStoreLocation(Store store) {
        return storeLocationRepository.findByStore(store)
            .orElseGet(() -> createDefaultLocation(store));
    }

    // 기본 위치 생성 (서울)
    private StoreLocation createDefaultLocation(Store store) {
        log.warn("StoreLocation not found for store: {}, using default Seoul location", store.getStoreId());
        return StoreLocation.builder()
            .store(store)
            .latitude(37.5665)
            .longitude(126.9780)
            .city("서울특별시")
            .weatherRegionCode("Seoul")
            .build();
    }

    // Store 엔티티 조회 (구현 필요)
    private Store findStore(Long storeId) {
        // TODO: StoreRepository에서 storeId로 Store 조회 구현
        throw new UnsupportedOperationException("Store 조회 로직을 구현하세요.");
    }

    // 현재 계절 반환 (구현 필요)
    private String getCurrentSeason() {
        // TODO: 현재 날짜 기준 계절 반환 로직 구현
        return "SUMMER";
    }
}
