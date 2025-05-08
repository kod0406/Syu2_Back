package com.example.demo.entity.store;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreMenu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId; // 고유 ID 필요

    private String menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    private Store store;
}
