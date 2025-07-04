package com.example.demo.store.service;

import com.example.demo.store.entity.StoreLocation;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreLocationService {
    private final StoreLocationRepository storeLocationRepository;

    @Transactional
    public StoreLocation saveOrUpdateLocation(Store store, String fullAddress, String city, String district, Double latitude, Double longitude) {
        StoreLocation location = storeLocationRepository.findByStore(store)
                .orElse(StoreLocation.builder().store(store).build());
        location.updateLocation(fullAddress, city, district, latitude, longitude);
        return storeLocationRepository.save(location);
    }

    // 매장별 위치 정보 조회 (추가)
    public Optional<StoreLocation> findByStore(Store store) {
        return storeLocationRepository.findByStore(store);
    }

    // 매장 삭제 시 위치 정보도 함께 삭제
    @Transactional
    public void deleteByStore(Store store) {
        storeLocationRepository.findByStore(store)
            .ifPresent(storeLocationRepository::delete);
    }
}
