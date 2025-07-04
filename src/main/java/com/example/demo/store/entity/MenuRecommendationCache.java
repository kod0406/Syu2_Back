package com.example.demo.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"store_id", "weather_condition", "season"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class MenuRecommendationCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(columnDefinition = "TEXT")
    private String gptRecommendation;     // GPT API 응답 JSON

    private String weatherCondition;      // "RAINY", "CLEAR", "COLD", "HOT"
    private String season;               // "SPRING", "SUMMER", "FALL", "WINTER"

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiredAt;     // 캐시 만료 시간 (3-6시간 후)

    @Builder.Default
    private boolean isActive = true;     // 활성 상태

    // 캐시 만료 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    // 캐시 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    // 현재 계절 반환
    public static String getCurrentSeason() {
        int month = LocalDateTime.now().getMonthValue();

        if (month >= 3 && month <= 5) return "SPRING";
        if (month >= 6 && month <= 8) return "SUMMER";
        if (month >= 9 && month <= 11) return "FALL";
        return "WINTER";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
