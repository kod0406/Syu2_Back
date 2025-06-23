package com.example.demo.socialLogin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor //역직렬화를 위한 기본 생성자
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "카카오 인증 토큰 응답 DTO")
public class KakaoTokenResponseDto {
    /**
     * KakaoTokenResponseDto 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 선택/부가 정보
     *
     * tokenType               ✅  토큰 타입 (ex: bearer)
     * accessToken             ✅  액세스 토큰
     * idToken                 ⛔  OpenID Connect에서 사용
     * expiresIn               ✅  액세스 토큰 만료 시간 (초)
     * refreshToken            ✅  리프레시 토큰
     * refreshTokenExpiresIn   ✅  리프레시 토큰 만료 시간 (초)
     * scope                   ⛔  요청한 인증 범위 (ex: profile, email)
     */

    @JsonProperty("token_type")
    @Schema(description = "토큰 타입", example = "bearer")
    public String tokenType;

    @JsonProperty("access_token")
    @Schema(description = "액세스 토큰", example = "AAAANkQ-QepuuY_csMz-UoJP6x9kWPcw23D...")
    public String accessToken;

    @JsonProperty("id_token")
    @Schema(description = "ID 토큰", example = "eyJraWQiOiI5ZjI3NzM0YS...")
    public String idToken;

    @JsonProperty("expires_in")
    @Schema(description = "토큰 만료 시간(초)", example = "21599")
    public Integer expiresIn;

    @JsonProperty("refresh_token")
    @Schema(description = "리프레시 토큰", example = "c8ceMEJisO4Se7uGCEYKK1p52L93...")
    public String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    @Schema(description = "리프레시 토큰 만료 시간(초)", example = "5183999")
    public Integer refreshTokenExpiresIn;

    @JsonProperty("scope")
    @Schema(description = "인증 범위", example = "account_email profile")
    public String scope;
}
