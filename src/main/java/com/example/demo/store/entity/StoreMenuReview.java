package com.example.demo.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreMenuReview {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // StoreMenu의 PK를 이 엔티티의 PK로 사용
    @JoinColumn(name = "id") // 외래키 이름 = PK
    private StoreMenu storeMenu;

    @Column(name = "review_content")
    private String reviewContent; // reviewDetails -> reviewContent로 변경

    private String imageUrl;

    private double rating; // 평점 필드 추가 (1.0 ~ 5.0)

    @Builder.Default
    private LocalDateTime reviewDate = LocalDateTime.now(); // 리뷰 작성 날짜 필드 추가

    @PrePersist
    protected void onCreate() {
        if (reviewDate == null) {
            reviewDate = LocalDateTime.now();
        }
    }
}
