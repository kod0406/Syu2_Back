package com.example.demo.customer.entity;

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
public class CustomerReviewCollect {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double score;

    private String reviewDetails;

    // DB의 VARCHAR 타입과 매핑하기 위한 컨버터 사용
    @Column(name = "review_date")
    @Convert(converter = LocalDateStringConverter.class)
    private LocalDate reviewDate;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 외래키 이름
    @JsonBackReference
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    @JsonBackReference
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id") // 외래키 이름
    @JsonBackReference
    private StoreMenu storeMenu;
}
