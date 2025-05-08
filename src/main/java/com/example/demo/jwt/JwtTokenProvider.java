package com.example.demo.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // JWT 비밀 키 (base64 인코딩 또는 하드코딩된 키)
    @Value("${jwt.secret}")
    private String secretKeyEncoded;


    @Value("${jwt.expiration-hours}")
    private long expirationHours;

    private Key key;

    // secretKey → Key 객체로 변환 (애플리케이션 시작 시 실행)
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyEncoded.getBytes());
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID (문자열로 저장됨)
     * @return 생성된 JWT 토큰
     */
    public String createToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationHours * 60 * 60 * 1000L); // 시간 → 밀리초

        return Jwts.builder()
                .setClaims(Jwts.claims().setSubject(userId))
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
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
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
}
