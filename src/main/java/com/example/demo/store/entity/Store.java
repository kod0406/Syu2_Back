package com.example.demo.store.entity;

import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.order.entity.OrderStatus;
import com.example.demo.benefit.entity.Coupon;
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
public class Store implements AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long storeId;

    private String storeName;

    private String ownerEmail;

    private String password;

    private String provider;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QR_Code> qr_Code = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMenu> storeMenu = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatus> orderStatus = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerStatistics> customerStatistics = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coupon> coupons = new ArrayList<>();

    @Override
    public Long getId() {
        return storeId;
    }

    @Override
    public String getEmail() {
        return ownerEmail;
    }

    @Override
    public String getRole() {
        return "ROLE_STORE";
    }
    // 매장 이름으로 검색


}
