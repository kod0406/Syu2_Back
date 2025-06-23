package com.example.demo.customer.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomerPoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pointId;

    private long pointAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 외래키 이름
    private Customer customer;


    public void addPoint(long point) {
        this.pointAmount += point;
    }


    public void subtractPoint(int amount) {
        if (this.pointAmount < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        this.pointAmount -= amount;
    }
}
