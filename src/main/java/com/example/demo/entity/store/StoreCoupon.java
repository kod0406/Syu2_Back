package com.example.demo.entity.store;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreCoupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    private String couponDetail;

    private LocalDate couponDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    private Store store;
}
