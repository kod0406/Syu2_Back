package com.example.demo.setting.jwt;

import com.example.demo.setting.jwt.TokenResponseDto;
import com.example.demo.setting.jwt.TokenService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "토큰 관리", description = "JWT 토큰 재발급 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    //private final com.example.demo.jwt.JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입
    private final com.example.demo.setting.jwt.JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입

    @Operation(summary = "액세스 토큰 재발급", description = "HttpOnly 쿠키로 전달된 리프레시 토큰을 사용하여 만료된 액세스 토큰을 재발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공. 새로운 액세스 토큰이 Body와 쿠키로 반환됩니다.",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 만료되었거나 유효하지 않습니다. 응답 Body의 redirectUrl로 재로그인이 필요합니다.",
                    content = @Content(schema = @Schema(example = "{\"message\": \"Refresh token is compromised or expired. Please login again.\", \"redirectUrl\": \"/customer/login\"}")))
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            TokenResponseDto tokenResponseDto = tokenService.refreshAccessToken(request);

            // 새로운 Access Token을 ResponseCookie를 사용하여 쿠키에 저장
            ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", tokenResponseDto.getAccessToken())
                    .path("/")
                    .httpOnly(false) // JavaScript에서 접근하도록
                    .maxAge(jwtTokenProvider.getAccessTokenExpirationSeconds()) // 초 단위로 만료 시간 설정
                    .sameSite("Lax") // CSRF 방지를 위한 SameSite 설정
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());


            return ResponseEntity.ok(tokenResponseDto);
        } catch (JwtException e) {
            // Refresh Token이 유효하지 않거나 문제가 있을 경우, 클라이언트가 재로그인하도록 유도
            // 기존 쿠키 삭제
            Cookie accessTokenCookie = new Cookie("access_token", null);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refresh_token", null);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);

            String role = "ROLE_CUSTOMER"; // 기본값
            if (request.getCookies() != null) {
                String refreshToken = Arrays.stream(request.getCookies())
                        .filter(c -> "refresh_token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
                if (refreshToken != null) {
                    String extractedRole = jwtTokenProvider.getRoleFromToken(refreshToken);
                    if (extractedRole != null) {
                        role = extractedRole;
                    }
                }
            }

            String redirectUrl = "ROLE_STORE".equals(role) ? "/owner/login" : "/customer/login";

            Map<String, String> body = new HashMap<>();
            body.put("message", e.getMessage());
            body.put("redirectUrl", redirectUrl);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }
}
