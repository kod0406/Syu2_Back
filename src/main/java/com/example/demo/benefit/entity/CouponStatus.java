package com.example.demo.benefit.entity;

public enum CouponStatus {
    ACTIVE, // 사용 가능한 쿠폰
    INACTIVE, // 사용 불가능한 쿠폰 (발급 중지 등)
    RECALLED, // 회수된 쿠폰 (사용 불가)
    UNUSED, // 미사용
    USED,   // 사용됨
    EXPIRED  // 만료됨
}
    