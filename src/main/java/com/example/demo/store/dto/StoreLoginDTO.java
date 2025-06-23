package com.example.demo.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "매장 로그인 요청 DTO")
public class StoreLoginDTO {
    @Schema(description = "점주 이메일", example = "owner@example.com", required = true)
    private String ownerEmail;
    
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
}
