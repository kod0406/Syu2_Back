package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Schema(description = "매장 회원가입 요청 DTO", example = "{\n  \"storeName\": \"맛있는 식당\",\n  \"ownerEmail\": \"owner@example.com\",\n  \"password\": \"password123\"\n}")
public class StoreRegistrationDTO {
    @Schema(description = "매장 이름", example = "맛있는 식당")
    private String storeName;
    
    @Schema(description = "점주 이메일", example = "owner@example.com")
    private String ownerEmail;
    
    @Schema(description = "비밀번호", example = "password123")
    private String password; //추후 추가
}
