package com.example.demo.store.service;

import com.example.demo.store.entity.StoreLocation;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

