package com.example.demo.setting.jwt;

import com.example.demo.setting.jwt.TokenResponseDto;
import com.example.demo.setting.jwt.JwtTokenProvider;
import com.example.demo.setting.util.TokenRedisService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    public TokenResponseDto refreshAccessToken(HttpServletRequest request) {
        String refreshTokenFromCookie = extractRefreshToken(request);

        if (refreshTokenFromCookie == null) {
            throw new JwtException("Refresh token not found in cookie.");
        }

        if (!jwtTokenProvider.validateToken(refreshTokenFromCookie)) {
            throw new JwtException("Invalid refresh token.");
        }

        String userId = jwtTokenProvider.getUserId(refreshTokenFromCookie);
        String role = jwtTokenProvider.getRole(refreshTokenFromCookie);
        String refreshTokenFromRedis = tokenRedisService.getRefreshToken(userId);

        if (refreshTokenFromRedis == null || !refreshTokenFromRedis.equals(refreshTokenFromCookie)) {
            tokenRedisService.deleteRefreshToken(userId); // Clean up Redis
            throw new JwtException("Refresh token is compromised or expired. Please login again.");
        }

        String newAccessToken = jwtTokenProvider.createToken(userId, role);
        return new TokenResponseDto(newAccessToken);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
