package com.example.demo.customer.entity;

import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.CouponStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class CustomerCoupon {
    @Id
    @Column(unique = true, length = 36)
    private String couponUuid; // UUID가 PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon; // Coupon 엔티티의 PK(Long) 참조

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // 쿠폰을 발급받은 고객

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus couponStatus = CouponStatus.UNUSED; // 쿠폰 사용상태

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt; //CleanUp 쓰려면 이게 필요함.

    public void markAsUsed() {
        this.couponStatus = CouponStatus.USED;
    }
}
