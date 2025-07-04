package com.example.demo.geolocation;

import com.example.demo.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class GeoResponseStoreDto {
    String storeName;
    long storeId;
    public GeoResponseStoreDto(Store store) {
        this.storeName = store.getStoreName();
        this.storeId = store.getStoreId();
    }
}
