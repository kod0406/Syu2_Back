package com.example.demo.entity.coupon;

import jakarta.persistence.Entity;

@Entity
public class CouponDetail {

    @Column(nullable = false, unique = true)
    private String couponUuid; // UUID 문자열 형태로 저장

    @Column(name = "coupon_code", nullable = false)
    private String couponCode; // DB에 존재하는 coupon_code 필드
}
