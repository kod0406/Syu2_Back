package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class CustomerStatisticsDto {
    private String menuName;
    private String imageUrl;
    private Long totalQuantity;
    private Long totalRevenue;

}
