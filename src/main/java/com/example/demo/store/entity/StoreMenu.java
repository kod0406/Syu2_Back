package com.example.demo.store.entity;

import com.example.demo.customer.entity.CustomerReviewCollect;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreMenu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId; // 고유 ID 필요

    private String menuName;

    //가격
    private Integer price;
    //가능 여부
    private Boolean available;
    //별점
    @Column(nullable = false)
    @Builder.Default
    private Double rating = 0.0;
    //일 판매량
    @Builder.Default
    private Integer dailySales = 0;
    // 전체 판매량
    @Builder.Default
    private Integer totalSales = 0;
    //설명
    private String description;
    //일일 수익
    @Builder.Default
    private Long dailyRevenue = 0L;
    //수익
    @Builder.Default
    private Long revenue = 0L;
    //사진
    private String imageUrl;

    private String category;

    @Column(nullable = false)
    @Builder.Default
    private long ratingCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    @JsonBackReference("store-menu")
    private Store store;

    @OneToMany(mappedBy = "storeMenu", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference("menu-reviews")
    private List<CustomerReviewCollect> reviews = new ArrayList<>();


    public void updateMenu(String menuName, Integer price, String description, String imageUrl, Boolean available, String category) {
        this.menuName = menuName;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
        this.category = category;
    }
    // 별점 추가 메서드
    public void updateRating(Double newScore) {
        if (this.rating == null) this.rating = 0.0;

        this.rating = (this.rating * this.ratingCount + newScore) / (this.ratingCount + 1);
        this.ratingCount++;
    }

}
