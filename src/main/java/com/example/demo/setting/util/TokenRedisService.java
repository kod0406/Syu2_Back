package com.example.demo.setting.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    /**
     * 리프레시 토큰을 Redis에 저장
     * @param userId 사용자 ID (Key로 사용)
     * @param refreshToken 저장할 리프레시 토큰
     * @param expirationMillis 토큰 만료 시간 (밀리초)
     */
    public void saveRefreshToken(String userId, String refreshToken, long expirationMillis) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Redis에서 리프레시 토큰 조회
     * @param userId 사용자 ID
     * @return 저장된 리프레시 토큰
     */
    public String getRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Redis에서 리프레시 토큰 삭제
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }
}

