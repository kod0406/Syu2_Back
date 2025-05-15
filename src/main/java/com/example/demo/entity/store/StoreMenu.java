package com.example.demo.entity.store;

import jakarta.persistence.*;
import lombok.*;

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
    private Double rating;
    //일 판매량
    private Integer dailySales;
    //설명
    private String description;
    //수익
    private Long revenue;
    //사진
    private String imageUrl;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    private Store store;

    public void updateMenu(String menuName, Integer price, String description, String imageUrl, Boolean available) {
        this.menuName = menuName;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
    }
}
