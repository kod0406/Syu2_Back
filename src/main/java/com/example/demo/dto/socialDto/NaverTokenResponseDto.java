package com.example.demo.dto.socialDto;

import lombok.Data;

@Data
public class NaverTokenResponseDto {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private String expires_in;
}
