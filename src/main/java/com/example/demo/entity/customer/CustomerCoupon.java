package com.example.demo.entity.customer;

import com.example.demo.entity.coupon.Coupon;
import com.example.demo.entity.coupon.CouponStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomerCoupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private LocalDateTime issuedAt; // 발급일시

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 만료일시

    @Column(nullable = false)
    private boolean isUsed = false; // 사용여부

    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        if (this.coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalStateException("현재 사용할 수 없는 쿠폰입니다.");
        }
        this.isUsed = true;
    }
}
