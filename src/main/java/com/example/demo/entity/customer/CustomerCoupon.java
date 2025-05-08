package com.example.demo.entity.customer;

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
public class CustomerCoupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    private LocalDate couponDate;

    private Boolean couponActive;

    private String couponDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 외래키 이름
    private Customer customer;

}
