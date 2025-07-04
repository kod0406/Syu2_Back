package com.example.demo.external.weather.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
    // 위도/경도 DTO
public class Coordinates {
    private double lat;
    private double lon;
}
