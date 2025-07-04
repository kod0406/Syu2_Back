package com.example.demo.customer.entity;
import com.example.demo.order.entity.OrderGroup;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    @JsonBackReference
    private OrderGroup orderGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 외래키 이름
    @JsonBackReference
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    @JsonBackReference("store-customer-statistics")
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    @JsonBackReference
    private StoreMenu storeMenu;

    public void markAsReviewed() {
        this.reviewed = true;
    }

    public void setOrderGroup(){

    }

}
