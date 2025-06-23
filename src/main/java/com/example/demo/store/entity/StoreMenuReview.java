package com.example.demo.store.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private String reviewDetails;

    private String imageUrl;
}
