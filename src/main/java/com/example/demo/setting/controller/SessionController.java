package com.example.demo.setting.controller;

import com.example.demo.setting.util.TokenRedisService;
import com.example.demo.setting.jwt.JwtTokenProvider;
import com.example.demo.store.entity.Store;
import com.example.demo.customer.entity.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@Tag(name = "세션 관리", description = "사용자 세션 및 토큰 무효화 관련 API")
public class SessionController {

    private final TokenRedisService tokenRedisService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "현재 세션 정보 조회", description = "현재 로그인된 사용자의 세션 정보를 조회합니다.")
    @SecurityRequirement(name = "access_token")
    @GetMapping("/info")
    public ResponseEntity<?> getSessionInfo(@AuthenticationPrincipal Object user) {
        try {
            String userId = null;
            String userType = null;

            if (user instanceof Store) {
                Store store = (Store) user;
                userId = store.getOwnerEmail();
                userType = "STORE";
            } else if (user instanceof Customer) {
                Customer customer = (Customer) user;
                userId = customer.getEmail();
                userType = "CUSTOMER";
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "인증되지 않은 사용자입니다.",
                    "success", false
                ));
            }

            String sessionInfo = tokenRedisService.getSessionInfo(userId);
            boolean hasActiveSession = tokenRedisService.hasActiveSession(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("userType", userType);
            response.put("hasActiveSession", hasActiveSession);
            response.put("sessionInfo", sessionInfo);

            if (sessionInfo != null) {
                // 세션 정보 파싱
                String[] parts = sessionInfo.split(",");
                Map<String, String> parsedInfo = new HashMap<>();
                
                for (String part : parts) {
                    if (part.contains(":")) {
                        String[] keyValue = part.split(":", 2);
                        parsedInfo.put(keyValue[0], keyValue[1]);
                    }
                }
                response.put("parsedSessionInfo", parsedInfo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[세션 정보 조회 오류] 예외: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "세션 정보 조회 중 오류가 발생했습니다.",
                "success", false
            ));
        }
    }

    @Operation(summary = "다른 기기 강제 로그아웃", description = "현재 사용자의 다른 모든 기기에서 강제 로그아웃을 실행합니다.")
    @SecurityRequirement(name = "access_token")
    @PostMapping("/force-logout-others")
    public ResponseEntity<?> forceLogoutOtherDevices(
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        try {
            String userId = null;
            String userRole = null;

            if (user instanceof Store) {
                Store store = (Store) user;
                userId = store.getOwnerEmail();
                userRole = "ROLE_STORE";
            } else if (user instanceof Customer) {
                Customer customer = (Customer) user;
                userId = customer.getEmail();
                userRole = "ROLE_CUSTOMER";
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "인증되지 않은 사용자입니다.",
                    "success", false
                ));
            }

            // 현재 기기 정보 추출
            String userAgent = request.getHeader("User-Agent");
            String clientIp = getClientIpAddress(request);
            String currentDeviceInfo = String.format("IP:%s,UA:%s", clientIp, 
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown");

            // 기존 세션 강제 무효화
            tokenRedisService.forceInvalidateAllSessions(userId, "사용자 요청에 의한 다른 기기 강제 로그아웃");

            // 새로운 토큰 생성
            String newAccessToken = jwtTokenProvider.createToken(userId, userRole);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, userRole);

            // 새 세션 생성
            long refreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationMillis();
            tokenRedisService.saveRefreshToken(userId, newRefreshToken, refreshTokenExpirationMillis, currentDeviceInfo);

            log.info("🔒 사용자 요청에 의한 다른 기기 강제 로그아웃 완료 - 사용자: {}", userId);

            return ResponseEntity.ok(Map.of(
                "message", "다른 기기에서의 로그인이 모두 해제되었습니다.",
                "success", true,
                "newAccessToken", newAccessToken,
                "currentDevice", currentDeviceInfo
            ));

        } catch (Exception e) {
            log.error("[다른 기기 강제 로그아웃 오류] 예외: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "다른 기기 강제 로그아웃 처리 중 오류가 발생했습니다.",
                "success", false
            ));
        }
    }

    @Operation(summary = "토큰 상태 확인", description = "현재 액세스 토큰의 유효성을 확인합니다.")
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "토큰이 필요합니다.",
                    "valid", false
                ));
            }

            boolean isValid = jwtTokenProvider.validateToken(token);
            boolean isInvalidated = tokenRedisService.isTokenInvalidated(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid && !isInvalidated);
            response.put("invalidated", isInvalidated);

            if (isValid && !isInvalidated) {
                String userId = jwtTokenProvider.getUserId(token);
                String role = jwtTokenProvider.getRole(token);
                response.put("userId", userId);
                response.put("role", role);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[토큰 검증 오류] 예외: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "토큰 검증 중 오류가 발생했습니다."
            ));
        }
    }

    @Operation(summary = "세션 만료 알림", description = "다른 기기에서 로그인하여 현재 세션이 만료되었음을 확인합니다.")
    @PostMapping("/session-expired")
    public ResponseEntity<?> notifySessionExpired(@RequestBody Map<String, String> request,
                                                  HttpServletResponse response) {
        try {
            String reason = request.getOrDefault("reason", "다른 기기에서 로그인");
            
            // 쿠키 삭제
            ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("access_token", "")
                    .httpOnly(false)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessTokenCookie.toString());

            ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString());

            return ResponseEntity.ok(Map.of(
                "message", "세션이 만료되었습니다. " + reason,
                "expired", true,
                "redirectTo", "/login"
            ));

        } catch (Exception e) {
            log.error("[세션 만료 알림 오류] 예외: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "세션 만료 처리 중 오류가 발생했습니다.",
                "success", false
            ));
        }
    }

    @Operation(summary = "WebSocket 세션 연결", description = "WebSocket을 통한 실시간 세션 알림을 위한 연결 확인")
    @SecurityRequirement(name = "access_token")
    @PostMapping("/connect-websocket")
    public ResponseEntity<?> connectWebSocket(@AuthenticationPrincipal Object user) {
        try {
            String userId = null;
            String userType = null;

            if (user instanceof Store) {
                Store store = (Store) user;
                userId = store.getOwnerEmail();
                userType = "STORE";
            } else if (user instanceof Customer) {
                Customer customer = (Customer) user;
                userId = customer.getEmail();
                userType = "CUSTOMER";
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "인증되지 않은 사용자입니다.",
                    "success", false
                ));
            }

            // WebSocket 토픽 경로 생성
            String topicPath = "/topic/session/" + userId.replaceAll("[@.]", "_");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("userType", userType);
            response.put("topicPath", topicPath);
            response.put("message", "WebSocket 연결 정보가 성공적으로 제공되었습니다.");

            log.info("📡 WebSocket 연결 정보 제공 - 사용자: {}, 토픽: {}", userId, topicPath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[WebSocket 연결 정보 제공 오류] 예외: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "WebSocket 연결 정보 제공 중 오류가 발생했습니다.",
                "success", false
            ));
        }
    }

    /**
     * 클라이언트 실제 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_CLIENT_IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
