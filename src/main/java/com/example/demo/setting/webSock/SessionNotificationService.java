package com.example.demo.setting.webSock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 세션 무효화 알림을 특정 사용자에게 전송
     * @param userId 사용자 ID (이메일)
     * @param reason 무효화 사유
     * @param newDeviceInfo 새로 로그인한 기기 정보
     */
    public void notifySessionInvalidated(String userId, String reason, String newDeviceInfo) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SESSION_INVALIDATED");
            notification.put("message", "다른 기기에서 로그인하여 현재 세션이 만료되었습니다.");
            notification.put("reason", reason);
            notification.put("newDeviceInfo", parseDeviceInfo(newDeviceInfo));
            notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            notification.put("action", "FORCE_LOGOUT");

            // 특정 사용자에게만 알림 전송
            String destination = "/topic/session/" + userId.replaceAll("[@.]", "_");
            messagingTemplate.convertAndSend(destination, notification);

            log.info("🔔 세션 무효화 알림 전송 완료 - 사용자: {}, 목적지: {}", userId, destination);

        } catch (Exception e) {
            log.error("🚨 세션 무효화 알림 전송 실패 - 사용자: {}, 오류: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 새로운 기기 로그인 알림을 전송
     * @param userId 사용자 ID
     * @param deviceInfo 기기 정보
     */
    public void notifyNewDeviceLogin(String userId, String deviceInfo) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_DEVICE_LOGIN");
            notification.put("message", "새로운 기기에서 로그인이 감지되었습니다.");
            notification.put("deviceInfo", parseDeviceInfo(deviceInfo));
            notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            notification.put("action", "INFO");

            String destination = "/topic/session/" + userId.replaceAll("[@.]", "_");
            messagingTemplate.convertAndSend(destination, notification);

            log.info("🔔 새 기기 로그인 알림 전송 완료 - 사용자: {}", userId);

        } catch (Exception e) {
            log.error("🚨 새 기기 로그인 알림 전송 실패 - 사용자: {}, 오류: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 강제 로그아웃 알림을 전송
     * @param userId 사용자 ID
     * @param reason 강제 로그아웃 사유
     */
    public void notifyForceLogout(String userId, String reason) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "FORCE_LOGOUT");
            notification.put("message", "관리자에 의해 강제 로그아웃되었습니다.");
            notification.put("reason", reason);
            notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            notification.put("action", "FORCE_LOGOUT");

            String destination = "/topic/session/" + userId.replaceAll("[@.]", "_");
            messagingTemplate.convertAndSend(destination, notification);

            log.info("🔔 강제 로그아웃 알림 전송 완료 - 사용자: {}", userId);

        } catch (Exception e) {
            log.error("🚨 강제 로그아웃 알림 전송 실패 - 사용자: {}, 오류: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 기기 정보를 파싱하여 사용자 친화적인 형태로 변환
     * @param deviceInfo 원본 기기 정보
     * @return 파싱된 기기 정보
     */
    private Map<String, String> parseDeviceInfo(String deviceInfo) {
        Map<String, String> parsed = new HashMap<>();

        if (deviceInfo == null || deviceInfo.isEmpty()) {
            parsed.put("ip", "알 수 없음");
            parsed.put("browser", "알 수 없음");
            parsed.put("os", "알 수 없음");
            return parsed;
        }

        try {
            String[] parts = deviceInfo.split(",");
            String ip = "알 수 없음";
            String userAgent = "알 수 없음";

            for (String part : parts) {
                if (part.startsWith("IP:")) {
                    ip = part.substring(3);
                } else if (part.startsWith("UA:")) {
                    userAgent = part.substring(3);
                }
            }

            parsed.put("ip", ip);
            parsed.put("browser", extractBrowser(userAgent));
            parsed.put("os", extractOS(userAgent));
            parsed.put("userAgent", userAgent.length() > 100 ? userAgent.substring(0, 100) + "..." : userAgent);

        } catch (Exception e) {
            log.warn("기기 정보 파싱 실패: {}", deviceInfo);
            parsed.put("ip", "파싱 실패");
            parsed.put("browser", "알 수 없음");
            parsed.put("os", "알 수 없음");
        }

        return parsed;
    }

    private String extractBrowser(String userAgent) {
        if (userAgent == null) return "알 수 없음";
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome") && !ua.contains("edg")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Safari";
        if (ua.contains("edg")) return "Edge";
        if (ua.contains("opera")) return "Opera";
        return "기타 브라우저";
    }

    private String extractOS(String userAgent) {
        if (userAgent == null) return "알 수 없음";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os")) return "macOS";
        if (ua.contains("linux")) return "Linux";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        return "기타 OS";
    }
}
