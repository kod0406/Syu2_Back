package com.example.demo.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerStatisticsDto {
    /**
     * MenuSalesStatisticsDto 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 선택/부가 정보
     *
     * menuName       ✅  메뉴 이름
     * imageUrl       ⛔  메뉴 이미지 URL
     * totalQuantity  ✅  총 판매 수량
     * totalRevenue   ✅  총 매출 금액
     */
    private String menuName;
    private String imageUrl;
    private Long totalQuantity;
    private Long totalRevenue;

}
