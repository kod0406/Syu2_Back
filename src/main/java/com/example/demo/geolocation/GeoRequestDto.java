package com.example.demo.geolocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoRequestDto {
    String latitude;
    String longitude;
}
