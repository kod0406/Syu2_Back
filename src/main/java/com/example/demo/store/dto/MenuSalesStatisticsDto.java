package com.example.demo.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuSalesStatisticsDto {
    private String menuName;
    private String imageUrl;
    private Long totalQuantity;
    private Long totalRevenue;
}
