package com.example.demo.dto;

import com.example.demo.entity.store.StoreMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
@Schema(description = "메뉴 응답 DTO")
public class MenuResponseDto {
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
    
    @Schema(description = "카테고리", example = "버거")
    private String category;
}
