package com.example.demo.setting.jwt;

import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.repository.StoreRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    // JWT 비밀 키 (base64 인코딩 또는 하드코딩된 키)
    @Value("${jwt.secret}")
    private String secretKeyEncoded;


    @Value("${jwt.expiration-hours}")
    private long accessTokenExpirationMillis;

    @Getter
    @Value("${jwt.refresh}")
    private long refreshTokenExpirationMillis;

    private Key key;

    // secretKey → Key 객체로 변환 (애플리케이션 시작 시 실행)
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyEncoded.getBytes());
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID (문자열로 저장됨)
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
     * 토큰 유효성 검증
     * @param token JWT
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpirationHours() {
        return accessTokenExpirationMillis / (60 * 60 * 1000); // 밀리초를 시간 단위로 변환
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMillis / 1000;
    }

}
