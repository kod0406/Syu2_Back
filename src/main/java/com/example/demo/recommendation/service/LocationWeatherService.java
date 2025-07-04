package com.example.demo.recommendation.service;

import com.example.demo.external.weather.service.WeatherApiService;
import com.example.demo.external.weather.dto.WeatherResponse;
import com.example.demo.recommendation.dto.StoreWeatherInfo;
import com.example.demo.recommendation.enums.SeasonType;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreLocation;
import com.example.demo.store.repository.StoreLocationRepository;
import com.example.demo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class LocationWeatherService {
    private final WeatherApiService weatherApiService;
    private final StoreLocationRepository storeLocationRepository;
    private final StoreRepository storeRepository;

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
            .storeId(store.getStoreId())
            .storeName(store.getStoreName())
            .fullAddress(location.getFullAddress())  // 전체 주소 추가
            .city(location.getCity())
            .district(location.getDistrict())
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

    // Store 엔티티 조회
    private Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("해당 storeId의 Store가 존재하지 않습니다: " + storeId));
    }

    // 현재 계절 반환 (SeasonType enum 사용)
    private SeasonType getCurrentSeason() {
        return SeasonType.getCurrentSeason();
    }
}
