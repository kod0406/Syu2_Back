package com.example.demo.entity.store;

import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.common.OrderStatus;
import com.example.demo.entity.customer.CustomerCoupon;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Store {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long storeId;

    private String storeName;

    private String ownerEmail;

    private String provider;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QR_Code> qr_Code = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMenu> storeMenu = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreCoupon> storeCoupon = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatus> orderStatus = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerStatistics> customerStatistics = new ArrayList<>();

}
