package com.example.demo.setting.util;

import com.example.demo.setting.webSock.SessionNotificationService;
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
    private final SessionNotificationService sessionNotificationService;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String SESSION_INFO_PREFIX = "SI:";
    private static final String INVALIDATED_TOKEN_PREFIX = "INVALID:";
    private static final String ACCESS_TOKEN_PREFIX = "AT:";

    /**
     * 리프레시 토큰을 Redis에 저장하고 기존 세션 무효화
     * @param userId 사용자 ID (Key로 사용)
     * @param refreshToken 저장할 리프레시 토큰
     * @param expirationMillis 토큰 만료 시간 (밀리초)
     * @param deviceInfo 기기 정보 (선택사항)
     * @param accessToken 현재 발급된 액세스 토큰
     * @return 기존 세션이 있었는지 여부
     */
    public boolean saveRefreshToken(String userId, String refreshToken, long expirationMillis, String deviceInfo, String accessToken) {
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        String sessionInfoKey = SESSION_INFO_PREFIX + userId;
        String accessKey = ACCESS_TOKEN_PREFIX + userId;

        // 기존 토큰들 확인 및 무효화
        String existingRefreshToken = redisTemplate.opsForValue().get(refreshKey);
        String existingAccessToken = redisTemplate.opsForValue().get(accessKey);
        String existingSessionInfo = redisTemplate.opsForValue().get(sessionInfoKey);
        boolean hadPreviousSession = existingRefreshToken != null;

        if (hadPreviousSession) {
            log.warn("🔄 기존 세션 감지 - 사용자: {}, 기존 토큰들 무효화 진행", userId);

            // 기존 리프레시 토큰을 무효화 리스트에 추가 (24시간 보관)
            if (existingRefreshToken != null) {
                invalidateToken(existingRefreshToken, 24 * 60 * 60 * 1000L);
            }

            // 기존 액세스 토큰도 무효화 리스트에 추가 ★ 핵심!
            if (existingAccessToken != null) {
                invalidateToken(existingAccessToken, 24 * 60 * 60 * 1000L);
                log.info("🚫 기존 액세스 토큰도 무효화: {}", existingAccessToken.substring(0, Math.min(20, existingAccessToken.length())) + "...");
            }

            // 기존 세션 정보 로깅
            if (existingSessionInfo != null) {
                log.info("📱 기존 세션 정보: {}", existingSessionInfo);
            }

            // ★ WebSocket을 통한 실시간 알림 전송
            try {
                sessionNotificationService.notifySessionInvalidated(
                    userId,
                    "다른 기기에서 로그인",
                    deviceInfo
                );
                log.info("🔔 세션 무효화 WebSocket 알림 전송 완료 - 사용자: {}", userId);
            } catch (Exception e) {
                log.error("🚨 WebSocket 알림 전송 실패 - 사용자: {}, 오류: {}", userId, e.getMessage());
                // WebSocket 알림 실패해도 로그인 프로세스는 계속 진행
            }
        }

        // 새 토큰들 저장
        redisTemplate.opsForValue().set(refreshKey, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);

        // 새 액세스 토큰도 Redis에 저장
        if (accessToken != null) {
            redisTemplate.opsForValue().set(accessKey, accessToken, expirationMillis, TimeUnit.MILLISECONDS);
        }

        // 세션 정보 저장
        String sessionInfo = String.format("loginTime:%s,device:%s",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            deviceInfo != null ? deviceInfo : "unknown"
        );
        redisTemplate.opsForValue().set(sessionInfoKey, sessionInfo, expirationMillis, TimeUnit.MILLISECONDS);

        log.info("✅ 새 세션 생성 - 사용자: {}, 기기: {}", userId, deviceInfo);
        return hadPreviousSession;
    }

    /**
     * 기존 메서드 호환성 유지
     */
    public boolean saveRefreshToken(String userId, String refreshToken, long expirationMillis, String deviceInfo) {
        return saveRefreshToken(userId, refreshToken, expirationMillis, deviceInfo, null);
    }

    /**
     * 기존 메서드 호환성 유지
     */
    public void saveRefreshToken(String userId, String refreshToken, long expirationMillis) {
        saveRefreshToken(userId, refreshToken, expirationMillis, null, null);
    }

    /**
     * 토큰을 무효화 리스트에 추가
     * @param token 무효화할 토큰
     * @param expirationMillis 무효화 정보 보관 시간
     */
    public void invalidateToken(String token, long expirationMillis) {
        if (token != null && !token.isEmpty()) {
            String invalidKey = INVALIDATED_TOKEN_PREFIX + token;
            redisTemplate.opsForValue().set(invalidKey, "invalidated", expirationMillis, TimeUnit.MILLISECONDS);
            log.info("🚫 토큰 무효화 완료: {}", token.substring(0, Math.min(20, token.length())) + "...");
        }
    }

    /**
     * 토큰이 무효화되었는지 확인
     * @param token 확인할 토큰
     * @return 무효화 여부
     */
    public boolean isTokenInvalidated(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }
        String invalidKey = INVALIDATED_TOKEN_PREFIX + token;
        return redisTemplate.hasKey(invalidKey);
    }

    /**
     * 특정 사용자의 모든 세션 강제 무효화
     * @param userId 사용자 ID
     * @param reason 무효화 사유
     */
    public void forceInvalidateAllSessions(String userId, String reason) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String sessionInfoKey = SESSION_INFO_PREFIX + userId;
        String accessKey = ACCESS_TOKEN_PREFIX + userId;

        // 기존 토큰들 무효화
        String existingRefreshToken = redisTemplate.opsForValue().get(key);
        String existingAccessToken = redisTemplate.opsForValue().get(accessKey);

        if (existingRefreshToken != null) {
            invalidateToken(existingRefreshToken, 24 * 60 * 60 * 1000L); // 24시간 보관
        }

        if (existingAccessToken != null) {
            invalidateToken(existingAccessToken, 24 * 60 * 60 * 1000L); // 24시간 보관
        }

        // 세션 정보 삭제
        redisTemplate.delete(key);
        redisTemplate.delete(sessionInfoKey);
        redisTemplate.delete(accessKey);

        // ★ WebSocket을 통한 강제 로그아웃 알림 전송
        try {
            sessionNotificationService.notifyForceLogout(userId, reason);
            log.info("🔔 강제 로그아웃 WebSocket 알림 전송 완료 - 사용자: {}", userId);
        } catch (Exception e) {
            log.error("🚨 강제 로그아웃 WebSocket 알림 전송 실패 - 사용자: {}, 오류: {}", userId, e.getMessage());
        }

        log.warn("⚠️ 강제 세션 무효화 - 사용자: {}, 사유: {}", userId, reason);
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

    /**
     * 사용자의 기존 액세스 토큰들을 무효화
     * @param userId 사용자 ID
     * @param currentAccessToken 현재 새로 발급된 액세스 토큰 (무효화 제외)
     */
    public void invalidateExistingAccessTokens(String userId, String currentAccessToken) {
        String sessionInfoKey = SESSION_INFO_PREFIX + userId;
        String sessionInfo = redisTemplate.opsForValue().get(sessionInfoKey);

        if (sessionInfo != null && sessionInfo.contains("accessToken:")) {
            // 기존 세션 정보에서 액세스 토큰 추출
            String[] parts = sessionInfo.split(",");
            for (String part : parts) {
                if (part.startsWith("accessToken:")) {
                    String existingAccessTokenPrefix = part.substring("accessToken:".length());
                    // 기존 액세스 토큰은 prefix만 저장되어 있으므로, 실제 토큰 전체를 무효화하기 어려움
                    // 이 방법보다는 세션 정보에 전체 액세스 토큰을 저장하거나,
                    // 다른 방식으로 액세스 토큰을 추적해야 함
                    log.warn("⚠️ 기존 액세스 토큰 prefix 발견: {}", existingAccessTokenPrefix);
                }
            }
        }
    }
}
