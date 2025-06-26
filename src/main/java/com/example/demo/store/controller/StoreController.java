package com.example.demo.store.controller;

import com.example.demo.store.service.QrCodeService;
import com.example.demo.store.dto.StoreSalesResponseDto;
import com.example.demo.store.entity.QR_Code;
import com.example.demo.store.repository.QRCodeRepository;
import com.example.demo.store.repository.StoreRepository;
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
    private final StoreRepository storeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final QRCodeRepository qrCodeRepository;
    private final QrCodeService qrCodeService;
    private final MemberValidUtil memberValidUtil;
    private final TokenRedisService tokenRedisService;
    private final JwtCookieUtil jwtCookieUtil;


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
    public ResponseEntity<?> login(@RequestBody StoreLoginDTO loginDTO, HttpServletResponse response) {
        Store store = storeService.authenticateStore(loginDTO.getOwnerEmail(), loginDTO.getPassword());
        String ownerEmail = store.getOwnerEmail();

        // 토큰 생성
        String accessToken = jwtTokenProvider.createToken(ownerEmail, "ROLE_STORE");
        String refreshToken = jwtTokenProvider.createRefreshToken(ownerEmail, "ROLE_STORE");

        // 리프레시 토큰 저장 (Redis)
        long refreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationMillis();
        tokenRedisService.saveRefreshToken(ownerEmail, refreshToken, refreshTokenExpirationMillis);

        // 액세스 토큰 쿠키 설정 (static 메서드 대신 인스턴스 메서드 사용)
        ResponseCookie accessTokenCookie = jwtCookieUtil.createAccessTokenCookie(accessToken);
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
}
