package com.example.demo.store.controller;

import com.example.demo.store.service.QrCodeService;
import com.example.demo.store.dto.StoreSalesResponseDto;
import com.example.demo.store.entity.QR_Code;
import com.example.demo.store.repository.QRCodeRepository;
import com.example.demo.store.service.StoreService;
import com.example.demo.store.dto.StoreLoginDTO;
import com.example.demo.store.dto.StoreRegistrationDTO;
import com.example.demo.store.entity.Store;
import com.example.demo.setting.jwt.JwtTokenProvider;
import com.example.demo.setting.util.JwtCookieUtil;
import com.example.demo.setting.util.MemberValidUtil;
import com.example.demo.setting.util.TokenRedisService;
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "매장 관리", description = "매장 계정 관리 API")
public class StoreController {
    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final QRCodeRepository qrCodeRepository;
    private final QrCodeService qrCodeService;
    private final MemberValidUtil memberValidUtil;
    private final TokenRedisService tokenRedisService;
    private final JwtCookieUtil jwtCookieUtil;
    private final com.example.demo.setting.service.EmailService emailService;


    @Operation(
            summary = "매장 회원가입",
            description = "신규 매장을 등록합니다. 요청 본문은 `application/json` 형식이며, `StoreRegistrationDTO`의 구조를 따릅니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "매장 등록 정보입니다. `StoreRegistrationDTO` 스키마를 참조하세요.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StoreRegistrationDTO.class)
                    )
            )
    )

    @PostMapping("/register")
    public ResponseEntity<?> registerStore(@RequestBody StoreRegistrationDTO registrationDTO) {
        Store store = storeService.registerStore(registrationDTO);

        // 회원가입 환영 이메일 발송
        emailService.sendWelcomeEmail(
                store.getOwnerEmail(),
                store.getStoreName(),
                store.getOwnerEmail()
        );

        return ResponseEntity.ok(Map.of(
                "message", "매장 가입이 완료되었습니다.",
                "storeId", store.getId(),
                "email", store.getOwnerEmail()
        ));
    }


    @Operation(summary = "매장 회원탈퇴", description = "로그인된 매장 계정을 탈퇴 처리하고 쿠키를 삭제합니다.")
    @SecurityRequirement(name = "access_token")
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawStore(@Parameter(hidden = true) @AuthenticationPrincipal Store store,
                                           HttpServletResponse response) {
        memberValidUtil.validateIsStore(store);
        storeService.deleteStore(store.getId());
        ResponseCookie deleteCookie = JwtCookieUtil.deleteAccessTokenCookie();
        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }


    @Operation(
            summary = "매장 로그인",
            description = "매장 계정으로 로그인하고 JWT 토큰을 쿠키에 발급합니다. 요청 본문은 `application/json` 형식이며, `StoreLoginDTO`의 구조를 따릅니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 정보 (이메일, 비밀번호)입니다. `StoreLoginDTO` 스키마를 참조하세요.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StoreLoginDTO.class)
                    )
            )
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody StoreLoginDTO loginDTO,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        Store store = storeService.authenticateStore(loginDTO.getOwnerEmail(), loginDTO.getPassword());
        String ownerEmail = store.getOwnerEmail();

        // 기기 정보 추출
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        String deviceInfo = String.format("IP:%s,UA:%s", clientIp, userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown");

        // 토큰 생성
        String accessToken = jwtTokenProvider.createToken(ownerEmail, "ROLE_STORE");
        String refreshToken = jwtTokenProvider.createRefreshToken(ownerEmail, "ROLE_STORE");

        // ✅ 기존 세션 정보를 Redis 저장 전에 미리 조회
        boolean hadPreviousSession = tokenRedisService.hasActiveSession(ownerEmail);
        String previousSessionInfo = tokenRedisService.getSessionInfo(ownerEmail);

        // 리프레시 토큰 저장 (Redis) - 기존 세션 자동 무효화
        long refreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationMillis();
        tokenRedisService.saveRefreshToken(ownerEmail, refreshToken, refreshTokenExpirationMillis, deviceInfo);

        // 로그인 이메일 알림 발송
        if (hadPreviousSession && previousSessionInfo != null && !isSameDevice(previousSessionInfo, deviceInfo)) {
            // 다른 기기에서 로그인 시 보안 경고 이메일
            emailService.sendSuspiciousLoginAlert(
                    ownerEmail,
                    store.getStoreName(),
                    deviceInfo,
                    java.time.LocalDateTime.now()
            );
        } else {
            // 일반 로그인 알림 이메일
            emailService.sendLoginNotificationEmail(
                    ownerEmail,
                    store.getStoreName(),
                    deviceInfo,
                    java.time.LocalDateTime.now()
            );
        }

        // 액세스 토큰 쿠키 설정
        ResponseCookie accessTokenCookie = JwtCookieUtil.createAccessTokenCookie(accessToken);
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        // 리프레시 토큰 쿠키 설정 (HttpOnly)
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서 필수
                .domain("igo.ai.kr") // 도메인 명시적 설정
                .path("/")
                .maxAge(refreshTokenExpirationMillis / 1000) //밀리초를 초로 변환
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    @Operation(summary = "매장 로그아웃", description = "로그인된 매장 계정을 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = JwtCookieUtil.deleteAccessTokenCookie();
        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @Operation(summary = "매장 QR코드 다운로드", description = "매장 ID로 저장된 QR 코드를 다운로드합니다.")
    @GetMapping("/{storeId}/qrcode/download")
    public ResponseEntity<byte[]> downloadStoreQrCode(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @AuthenticationPrincipal Store store) throws IOException, WriterException {
        memberValidUtil.validateIsStore(store);
        QR_Code qrCode = qrCodeRepository.findByStoreStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("QR 코드가 생성되지 않았습니다."));
        String fullUrl = qrCode.getQR_Code();
        byte[] qrCodeBytes = qrCodeService.generateQrCodeBytes(fullUrl, 250, 250);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=qrcode.png")
                .body(qrCodeBytes);
    }

    @Operation(summary = "매장 QR코드 조회 (JSON)", description = "매장 ID로 저장된 QR 코드 정보를 JSON으로 반환합니다.")
    @GetMapping("/{storeId}/qrcode/json")
    public ResponseEntity<?> getStoreQrCodeJson(@Parameter(description = "매장 ID") @PathVariable Long storeId,
                                                @AuthenticationPrincipal Store store) throws IOException, WriterException {
        memberValidUtil.validateIsStore(store);
        QR_Code qrCode = qrCodeRepository.findByStoreStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("QR 코드가 생성되지 않았습니다."));
        String fullUrl = qrCode.getQR_Code();
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(fullUrl, 250, 250);
        Map<String, String> response = new HashMap<>();
        response.put("storeId", String.valueOf(storeId));
        response.put("qrCodeUrl", qrCode.getQR_Code());
        response.put("fullUrl", fullUrl);
        response.put("qrCodeBase64", qrCodeBase64);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가게 매출 정보 조회", description = "특정 가게의 일일/전체 총 매출 및 판매량을 조회합니다.")
    @GetMapping("/{storeId}/sales")
    public ResponseEntity<?> getStoreSales(@Parameter(description = "매장 ID") @PathVariable Long storeId,
                                           @AuthenticationPrincipal Store store) {
        memberValidUtil.validateIsStore(store);
        StoreSalesResponseDto storeSales = storeService.getStoreSales(storeId);
        return ResponseEntity.ok(storeSales);
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
                return ip.split(",")[0].trim(); // 첫 번째 IP만 사용
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 같은 기기인지 확인 (IP 주소와 User-Agent 조합으로 판단)
     */
    private boolean isSameDevice(String previousSessionInfo, String currentDeviceInfo) {
        try {
            // previousSessionInfo 형식: "loginTime:2025-06-26T14:30:00,device:IP:192.168.1.100,UA:..."
            String previousDevice = previousSessionInfo.substring(previousSessionInfo.indexOf("device:") + 7);

            // 이전 기기 정보 파싱
            String[] previousParts = previousDevice.split(",");
            String previousIp = "";
            String previousUA = "";

            for (String part : previousParts) {
                if (part.startsWith("IP:")) {
                    previousIp = part.substring(3);
                } else if (part.startsWith("UA:")) {
                    previousUA = part.substring(3);
                }
            }

            // 현재 기기 정보 파싱
            String[] currentParts = currentDeviceInfo.split(",");
            String currentIp = "";
            String currentUA = "";

            for (String part : currentParts) {
                if (part.startsWith("IP:")) {
                    currentIp = part.substring(3);
                } else if (part.startsWith("UA:")) {
                    currentUA = part.substring(3);
                }
            }

            // 기기 판별 로직
            boolean sameIP = previousIp.equals(currentIp);
            boolean similarUA = isSimilarUserAgent(previousUA, currentUA);

            // IP가 같고 User-Agent가 유사하면 같은 기기로 판단
            // 또는 User-Agent가 완전히 같으면 IP가 달라도 같은 기기로 판단 (모바일 환경 고려)
            return (sameIP && similarUA) || isSameUserAgent(previousUA, currentUA);

        } catch (Exception e) {
            // 파싱 오류 시 다른 기기로 간주 (보안상 안전한 선택)
            return false;
        }
    }

    /**
     * User-Agent가 완전히 같은지 확인
     */
    private boolean isSameUserAgent(String ua1, String ua2) {
        if (ua1 == null || ua2 == null) return false;
        return ua1.equals(ua2);
    }

    /**
     * User-Agent가 유사한지 확인 (브라우저와 OS 기준)
     */
    private boolean isSimilarUserAgent(String ua1, String ua2) {
        if (ua1 == null || ua2 == null || ua1.equals("unknown") || ua2.equals("unknown")) {
            return false;
        }

        // 주요 브라우저 식별자 추출
        String browser1 = extractBrowser(ua1);
        String browser2 = extractBrowser(ua2);

        // 운영체제 식별자 추출
        String os1 = extractOS(ua1);
        String os2 = extractOS(ua2);

        // 브라우저와 OS가 모두 같으면 유사한 기기로 판단
        return browser1.equals(browser2) && os1.equals(os2);
    }

    /**
     * User-Agent에서 브라우저 정보 추출
     */
    private String extractBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome")) return "chrome";
        if (ua.contains("firefox")) return "firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "safari";
        if (ua.contains("edge")) return "edge";
        if (ua.contains("opera")) return "opera";
        return "unknown";
    }

    /**
     * User-Agent에서 운영체제 정보 추출
     */
    private String extractOS(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "windows";
        if (ua.contains("mac os")) return "macos";
        if (ua.contains("linux")) return "linux";
        if (ua.contains("android")) return "android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "ios";
        return "unknown";
    }
}
