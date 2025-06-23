package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "가게 매출 정보 응답 DTO")
public class StoreSalesResponseDto {

    @Schema(description = "가게 ID", example = "1")
    private Long storeId;

    @Schema(description = "일일 총 매출", example = "150000")
    private long dailyTotalRevenue;

    @Schema(description = "전체 총 매출", example = "1500000")
    private long totalRevenue;

    @Schema(description = "일일 총 판매량", example = "25")
    private int dailyTotalSales;

    @Schema(description = "전체 총 판매량", example = "250")
    private int totalSales;
}

