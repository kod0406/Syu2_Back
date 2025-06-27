package com.example.demo.setting.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String SESSION_INFO_PREFIX = "SI:";

    /**
     * 리프레시 토큰을 Redis에 저장하고 기존 세션 무효화
     * @param userId 사용자 ID (Key로 사용)
     * @param refreshToken 저장할 리프레시 토큰
     * @param expirationMillis 토큰 만료 시간 (밀리초)
     * @param deviceInfo 기기 정보 (선택사항)
     */
    public void saveRefreshToken(String userId, String refreshToken, long expirationMillis, String deviceInfo) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String sessionInfoKey = SESSION_INFO_PREFIX + userId;

        // 기존 토큰 확인
        String existingToken = redisTemplate.opsForValue().get(key);
        if (existingToken != null) {
            log.info("🔄 기존 세션 감지 - 사용자: {}, 새 로그인으로 기존 세션 무효화", userId);
        }

        // 새 토큰 저장 (기존 토큰 덮어쓰기)
        redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);

        // 세션 정보 저장
        String sessionInfo = String.format("loginTime:%s,device:%s",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            deviceInfo != null ? deviceInfo : "unknown"
        );
        redisTemplate.opsForValue().set(sessionInfoKey, sessionInfo, expirationMillis, TimeUnit.MILLISECONDS);

        log.info("✅ 새 세션 생성 - 사용자: {}, 기기: {}", userId, deviceInfo);
    }

    /**
     * 기존 메서드 호환성 유지
     */
    public void saveRefreshToken(String userId, String refreshToken, long expirationMillis) {
        saveRefreshToken(userId, refreshToken, expirationMillis, null);
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
     * 세션 정보 조회
     * @param userId 사용자 ID
     * @return 세션 정보
     */
    public String getSessionInfo(String userId) {
        String sessionInfoKey = SESSION_INFO_PREFIX + userId;
        return redisTemplate.opsForValue().get(sessionInfoKey);
    }

    /**
     * Redis에서 리프레시 토큰 및 세션 정보 삭제
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String sessionInfoKey = SESSION_INFO_PREFIX + userId;

        redisTemplate.delete(key);
        redisTemplate.delete(sessionInfoKey);
        log.info("🚪 로그아웃 - 사용자: {} 세션 삭제 완료", userId);
    }

    /**
     * 활성 세션 확인
     * @param userId 사용자 ID
     * @return 활성 세션 존재 여부
     */
    public boolean hasActiveSession(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.hasKey(key);
    }
}
