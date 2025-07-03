package com.example.demo.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class StoreLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private String fullAddress;        // 전체 주소
    private String city;              // 시/도
    private String district;          // 구/군
    private Double latitude;          // 위도 (카카오맵 API에서 받아온 값)
    private Double longitude;         // 경도 (카카오맵 API에서 받아온 값)


    // 날씨 API 호출을 위한 지역 정보
    private String weatherRegionCode; // "Seoul", "Busan" 등

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // 업데이트 메서드
    public void updateLocation(String fullAddress, String city, String district,
                              Double latitude, Double longitude) {
        this.fullAddress = fullAddress;
        this.city = city;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = LocalDateTime.now();

        // 도시명으로 날씨 지역 코드 설정
        this.weatherRegionCode = extractWeatherRegionCode(city);
    }

    // 도시명에서 날씨 API용 지역 코드 추출
    private String extractWeatherRegionCode(String city) {
        if (city == null) return "Seoul"; // 기본값

        if (city.contains("서울")) return "Seoul";
        if (city.contains("부산")) return "Busan";
        if (city.contains("대구")) return "Daegu";
        if (city.contains("인천")) return "Incheon";
        if (city.contains("광주")) return "Gwangju";
        if (city.contains("대전")) return "Daejeon";
        if (city.contains("울산")) return "Ulsan";
        if (city.contains("경기")) return "Gyeonggi";
        if (city.contains("강원")) return "Gangwon";
        if (city.contains("충북") || city.contains("충청북도")) return "Chungbuk";
        if (city.contains("충남") || city.contains("충청남도")) return "Chungnam";
        if (city.contains("전북") || city.contains("전라북도")) return "Jeonbuk";
        if (city.contains("전남") || city.contains("전라남도")) return "Jeonnam";
        if (city.contains("경북") || city.contains("경상북도")) return "Gyeongbuk";
        if (city.contains("경남") || city.contains("경상남도")) return "Gyeongnam";
        if (city.contains("제주")) return "Jeju";

        return "Seoul"; // 기본값
    }
}
