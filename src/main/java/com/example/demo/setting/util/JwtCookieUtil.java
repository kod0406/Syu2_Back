package com.example.demo.setting.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class JwtCookieUtil {

    private static final String COOKIE_NAME = "access_token";
    private static final Duration MAX_AGE = Duration.ofHours(1);

    public static ResponseCookie createAccessTokenCookie(String jwt) {
        return ResponseCookie.from(COOKIE_NAME, jwt)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 필수
                .domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(MAX_AGE)
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 필수
                .domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(0) // 즉시 삭제
                .sameSite("Lax")
                .build();
    }
}
