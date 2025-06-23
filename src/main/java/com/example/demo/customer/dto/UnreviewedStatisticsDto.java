package com.example.demo.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnreviewedStatisticsDto {
    private Long statisticsId;
    private String storeName;
    private String orderDetails;
    private long orderPrice;
    private long orderAmount;
    private LocalDate date;
}
