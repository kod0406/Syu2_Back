package com.example.demo.entity.customer;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomerPoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pointId;

    private long pointAmount;

    private String savePoint;

    private String usePoint;

    private LocalDate usePointDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 외래키 이름
    private Customer customer;


}
