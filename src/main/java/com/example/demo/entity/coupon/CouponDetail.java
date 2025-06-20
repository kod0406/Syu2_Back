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
    @Column(nullable = false, unique = true, length = 36)
    private String couponUuid; // UUID가 PK

    @Column(name = "coupon_code", nullable = false)
    private String couponCode;

    @Setter
    @OneToOne(mappedBy = "couponDetail", fetch = FetchType.LAZY)
    private Coupon coupon;
}
