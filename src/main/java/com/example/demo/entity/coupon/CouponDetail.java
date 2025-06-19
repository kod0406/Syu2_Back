package com.example.demo.entity.coupon;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String couponUuid; // UUID 문자열 형태로 저장

    @Column(name = "coupon_code", nullable = false)
    private String couponCode; // DB에 존재하는 coupon_code 필드

    @OneToOne(mappedBy = "couponDetail", fetch = FetchType.LAZY)
    private Coupon coupon;
}
