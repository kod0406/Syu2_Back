package com.example.demo.entity.common;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.store.Store;
import com.example.demo.entity.store.StoreMenu;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomerStatistics {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDate date;

    private String orderDetails;

    private long orderPrice;

    private long orderAmount;

    private boolean reviewed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_group_id")
    private OrderGroup orderGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 외래키 이름
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    private Store store;


}
