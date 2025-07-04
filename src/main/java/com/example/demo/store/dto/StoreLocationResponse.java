package com.example.demo.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationResponse {
    private String fullAddress;

    public static StoreLocationResponse from(String fullAddress) {
        return StoreLocationResponse.builder()
            .fullAddress(fullAddress)
            .build();
    }
}
