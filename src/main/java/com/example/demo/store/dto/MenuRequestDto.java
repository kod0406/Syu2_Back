package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "메뉴 등록/수정 요청 DTO")
public class MenuRequestDto {
    /**
     * MenuRequestDto 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 선택
     *
     * menuName    ✅ 메뉴 이름
     * price       ✅ 가격
     * description ⛔ 메뉴 설명
     * imageUrl    ⛔ 이미지 URL
     * available   ⛔ 판매 가능 여부
     * category    ⛔ 메뉴 카테고리
     */
    @Schema(description = "메뉴 이름", example = "불고기 버거", required = true)
    private String menuName;
    
    @Schema(description = "가격", example = "8000", required = true)
    private int price;
    
    @Schema(description = "메뉴 설명", example = "맛있는 불고기를 넣은 프리미엄 버거")
    private String description;
    
    @Schema(description = "이미지 URL", example = "https://example.com/menu/burger.jpg")
    private String imageUrl;
    
    @Schema(description = "판매 가능 여부", example = "true")
    private boolean available;
    
    @Schema(description = "카테고리", example = "버거")
    private String category;
}
