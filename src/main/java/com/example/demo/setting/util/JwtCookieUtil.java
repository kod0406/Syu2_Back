package com.example.demo.setting.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtCookieUtil {

    private static final String COOKIE_NAME = "access_token";
    private static final Duration MAX_AGE = Duration.ofHours(1);
    private static final String REFRESH_TOKEN_NAME = "refresh_token";

    public static ResponseCookie createAccessTokenCookie(String jwt) {
        return ResponseCookie.from(COOKIE_NAME, jwt)
                .httpOnly(false) // JS에서 접근 가능하도록 변경
                .secure(true) // HTTPS 환경에서 필수
                //.domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(MAX_AGE)
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie createRefreshTokenCookie(String refreshToken, long expirationMillis) {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)  // 추가된 부분
                //.domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(expirationMillis / 1000) // 밀리초를 초로 변환
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 필수
                //.domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(0) // 즉시 삭제
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 필수
                //.domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(0) // 즉시 삭제
                .sameSite("Lax")
                .build();
    }
}
