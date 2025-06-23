package com.example.demo.order.entity;

import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 주문 식별자 (자동 생성)

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    private Long storeId;

    private boolean active;

    @OneToMany(mappedBy = "orderGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerStatistics> customerStatisticsList = new ArrayList<>();

    public void markAsCompleted() {
        this.active = true;
    }

}
