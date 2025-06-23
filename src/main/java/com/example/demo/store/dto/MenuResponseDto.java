package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "메뉴 응답 DTO")
public class MenuResponseDto {
    /**
     * MenuResponseDto 주요 필드 요약
     *
     * ✅ 필수 (일반적으로 항상 포함되는 정보)
     * ⛔ 선택 (상황에 따라 없을 수도 있음)
     *
     * menuId       ✅ 메뉴 ID
     * menuName     ✅ 메뉴 이름
     * price        ✅ 가격
     * rating       ⛔ 평점
     * description  ⛔ 메뉴 설명
     * imageUrl     ⛔ 이미지 URL
     * available    ✅ 주문 가능 여부
     * category     ⛔ 메뉴 카테고리
     */

    @Schema(description = "메뉴 ID", example = "1")
    private Long menuId;

    private Long storeId;

    @Schema(description = "메뉴 이름", example = "불고기 버거")
    private String menuName;
    
    @Schema(description = "가격", example = "8000")
    private int price;
    
    @Schema(description = "평점", example = "4.5")
    private double rating;
    
    @Schema(description = "메뉴 설명", example = "맛있는 불고기를 넣은 프리미엄 버거")
    private String description;
    
    @Schema(description = "이미지 URL", example = "https://example.com/menu/burger.jpg")
    private String imageUrl;

    @Schema(description = "주문 가능 여부", example = "true")
    private boolean available;
    
    @Schema(description = "카테고리", example = "버거")
    private String category;
}
