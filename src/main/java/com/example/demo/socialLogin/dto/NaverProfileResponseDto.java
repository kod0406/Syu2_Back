package com.example.demo.socialLogin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "네이버 프로필 응답 DTO")
public class NaverProfileResponseDto {
    @Schema(description = "결과 코드", example = "00")
    private String resultcode;
    
    @Schema(description = "결과 메시지", example = "success")
    private String message;
    
    @Schema(description = "네이버 사용자 정보")
    private NaverProfile response;

    @Data
    @Schema(description = "네이버 사용자 프로필 정보")
    public static class NaverProfile {
        @Schema(description = "네이버 고유 ID", example = "12345678")
        private String id;
        
        @Schema(description = "이메일", example = "user@naver.com")
        private String email;
        
        @Schema(description = "이름", example = "홍길동")
        private String name;
        
        @Schema(description = "닉네임", example = "길동이")
        private String nickname;
        
        @Schema(description = "성별", example = "M")
        private String gender;
        
        @Schema(description = "연령대", example = "20-29")
        private String age;
        
        @Schema(description = "생일", example = "06-15")
        private String birthday;
        
        @Schema(description = "출생연도", example = "1995")
        private String birthyear;
        
        @Schema(description = "프로필 이미지 URL", example = "https://ssl.pstatic.net/profile.jpg")
        private String profile_image;
        
        @Schema(description = "휴대전화번호", example = "010-1234-5678")
        private String mobile;
    }
}
