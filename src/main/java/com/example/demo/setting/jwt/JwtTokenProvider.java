package com.example.demo.setting.jwt;

import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.setting.util.TokenRedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final TokenRedisService tokenRedisService;
    
    @Value("${jwt.secret}")
    private String secretKeyEncoded;

    @Value("${jwt.expiration-hours}")
    @Getter
    private long accessTokenExpirationMillis;

    @Getter
    @Value("${jwt.refresh}")
    private long refreshTokenExpirationMillis;

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyEncoded.getBytes());
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID
     * @param role 사용자 역할
     * @return 생성된 JWT 토큰
     */
    public String createToken(String userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMillis);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMillis);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     * @param token JWT
     * @return 사용자 ID
     */
    public String getUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("sub", String.class);
    }

    public String getRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public String getRoleFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("role", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 강화된 토큰 유효성 검증 (Redis 무효화 확인 포함)
     * @param token JWT
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            // 1. 기본 JWT 토큰 검증
            Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token);
            
            // 2. Redis에서 토큰 무효화 상태 확인
            if (tokenRedisService.isTokenInvalidated(token)) {
                log.warn("🚫 무효화된 토큰 사용 시도: {}", token.substring(0, Math.min(20, token.length())) + "...");
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("⏰ 만료된 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("❌ 잘못된 토큰: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 기존 토큰 유효성 검증 (Redis 확인 없이)
     * @param token JWT
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateTokenWithoutRedisCheck(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 리프레시 토큰 검증 및 액세스 토큰 재발급
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰 또는 null
     */
    public String refreshAccessToken(String refreshToken) {
        try {
            // 리프레시 토큰 유효성 검증
            if (!validateTokenWithoutRedisCheck(refreshToken)) {
                log.warn("🚫 유효하지 않은 리프레시 토큰");
                return null;
            }

            String userId = getUserId(refreshToken);
            String role = getRole(refreshToken);

            // Redis에서 저장된 리프레시 토큰과 비교
            String storedRefreshToken = tokenRedisService.getRefreshToken(userId);
            if (!refreshToken.equals(storedRefreshToken)) {
                log.warn("🚫 저장된 리프레시 토큰과 불일치 - 사용자: {}", userId);
                return null;
            }

            // 새로운 액세스 토큰 생성
            return createToken(userId, role);
        } catch (Exception e) {
            log.error("❌ 액세스 토큰 재발급 실패: {}", e.getMessage());
            return null;
        }
    }

    public long getAccessTokenExpirationHours() {
        return accessTokenExpirationMillis / (60 * 60 * 1000);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMillis / 1000;
    }
}
