package com.example.demo.geolocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleAddressDto {
    private String city;        // 시/도 (서울특별시, 전라남도 등)
    private String district;    // 군/구 (노원구, 광양시 등)
    private String town;        // 동/읍/면 (공릉동, 광양읍 등)
}
