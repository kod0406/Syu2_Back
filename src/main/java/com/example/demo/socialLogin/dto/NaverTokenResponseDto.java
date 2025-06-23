package com.example.demo.socialLogin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "네이버 인증 토큰 응답 DTO")
public class NaverTokenResponseDto {
    @Schema(description = "액세스 토큰", example = "AAAANkQ-QepuuY_csMz-UoJP6x9kWPcw23D...")
    private String access_token;
    
    @Schema(description = "리프레시 토큰", example = "c8ceMEJisO4Se7uGCEYKK1p52L93...")
    private String refresh_token;
    
    @Schema(description = "토큰 타입", example = "bearer")
    private String token_type;
    
    @Schema(description = "토큰 만료 시간(초)", example = "3600")
    private String expires_in;
}
