package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "메뉴 판매 정보 응답 DTO")
public class MenuSalesResponseDto {

    @Schema(description = "메뉴 ID", example = "1")
    private Long menuId;

    @Schema(description = "메뉴 이름", example = "불고기 버거")
    private String menuName;

    @Schema(description = "일일 판매량", example = "10")
    private int dailySales;

    @Schema(description = "총 판매량", example = "100")
    private int totalSales;

    @Schema(description = "일일 매출", example = "80000")
    private long dailyRevenue;

    @Schema(description = "총 매출", example = "800000")
    private long totalRevenue;
}

