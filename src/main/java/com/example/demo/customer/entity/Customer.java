package com.example.demo.customer.entity;

import com.example.demo.order.entity.OrderStatus;
import com.example.demo.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter // 스웨거 테스트용 임시 Setter
public class Customer implements AppUser {

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

    @Override
    public Long getId() {
        return customerId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getRole() {
        return "ROLE_CUSTOMER";
    }

}
