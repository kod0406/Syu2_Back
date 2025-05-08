package com.example.demo.entity.customer;

import com.example.demo.entity.common.CustomerReviewCollect;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.common.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long customerId;

    private String email;

    private String provider;

    private String nickName;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerPoint> customerPoint = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerCoupon> customerCoupon = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatus> orderStatus = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReviewCollect> customerReviewCollect = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerStatistics> customerStatistics = new ArrayList<>();
}
