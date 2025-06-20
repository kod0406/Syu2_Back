package com.example.demo.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class JwtCookieUtil {

    private static final String COOKIE_NAME = "access_token";
    private static final Duration MAX_AGE = Duration.ofHours(1);

    public static ResponseCookie createAccessTokenCookie(String jwt) {
        return ResponseCookie.from(COOKIE_NAME, jwt)
                .httpOnly(true)
                .path("/")
                .maxAge(MAX_AGE)
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0) // 즉시 삭제
                .sameSite("Lax")
                .build();
    }
}
