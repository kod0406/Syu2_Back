package com.example.demo.Controller;

import com.example.demo.Service.QrCodeTestService;
import com.example.demo.entity.store.QR_Code;
import com.example.demo.repository.QRCodeRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.Service.StoreService;
import com.example.demo.dto.StoreLoginDTO;
import com.example.demo.dto.StoreRegistrationDTO;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.entity.store.Store;
import com.example.demo.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@RestController
@RequestMapping("/api/stores")
@Tag(name = "매장 관리", description = "매장 계정 관리 API")
public class StoreController {
    private final StoreService storeService;
    private final StoreRepository storeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final QRCodeRepository qrCodeRepository;
    private final QrCodeTestService qrCodeTestService;

    @Autowired
    public StoreController(StoreService storeService,
                           StoreRepository storeRepository,
                           QRCodeRepository qrCodeRepository,
                           QrCodeTestService qrCodeTestService,
                           JwtTokenProvider jwtTokenProvider) {
        this.storeService = storeService;
        this.storeRepository = storeRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.qrCodeTestService = qrCodeTestService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

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
    public ResponseEntity<?> registerStore(
            @RequestBody StoreRegistrationDTO registrationDTO) {
        try {
            Store store = storeService.registerStore(registrationDTO);
            return ResponseEntity.ok(Map.of(
                    "message", "매장 가입이 완료되었습니다.",
                    "storeId", store.getId(),
                    "email", store.getOwnerEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "매장 가입 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "매장 회원탈퇴", description = "로그인된 매장 계정을 탈퇴 처리하고 쿠키를 삭제합니다.")
    @SecurityRequirement(name = "bearer-key")
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawStore(@Parameter(hidden = true) @AuthenticationPrincipal AppUser user,
                                           HttpServletResponse response) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            storeService.deleteStore(user.getId());

            Cookie cookie = new Cookie("access_token", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "회원 탈퇴 중 오류가 발생했습니다."));
        }
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
    public ResponseEntity<?> login(
            @RequestBody StoreLoginDTO loginDTO,
            HttpServletResponse response) {
        try {
            Store store = storeService.authenticateStore(loginDTO.getOwnerEmail(), loginDTO.getPassword());

            String token = jwtTokenProvider.createToken(store.getOwnerEmail());

            Cookie cookie = new Cookie("access_token", token);
            cookie.setMaxAge(3600);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of(
                    "message", "로그인 성공",
                    "token", token,
                    "storeId", store.getStoreId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "매장 로그아웃", description = "로그인된 매장 계정을 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            Cookie cookie = new Cookie("access_token", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "매장 QR코드 조회", description = "매장 ID로 저장된 QR 코드를 조회합니다.")
    @GetMapping("/{storeId}/qrcode")
    public ModelAndView getStoreQrCode(
            @Parameter(description = "매장 ID") @PathVariable Long storeId) {

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

            QR_Code qrCode = qrCodeRepository.findByStoreStoreId(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("QR 코드가 생성되지 않았습니다."));

            String qrCodeUrl = qrCode.getQR_Code();

            String fullUrl = qrCodeUrl; // 수정 후: qrCodeUrl이 이미 완전한 URL임

            String qrCodeBase64 = qrCodeTestService.generateQrCodeBase64(fullUrl, 250, 250);

            ModelAndView modelAndView = new ModelAndView("qr-result");
            modelAndView.addObject("qrCodeImage", "data:image/png;base64," + qrCodeBase64);
            modelAndView.addObject("url", fullUrl);

            return modelAndView;

        } catch (Exception e) {
            ModelAndView errorView = new ModelAndView("error");
            errorView.addObject("errorMessage", "QR 코드 조회 중 오류가 발생했습니다: " + e.getMessage());
            return errorView;
        }
    }

    @Operation(summary = "매장 QR코드 다운로드", description = "매장 ID로 저장된 QR 코드를 다운로드합니다.")
    @GetMapping("/{storeId}/qrcode/download")
    public ResponseEntity<byte[]> downloadStoreQrCode(
            @Parameter(description = "매장 ID") @PathVariable Long storeId) {

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

            QR_Code qrCode = qrCodeRepository.findByStoreStoreId(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("QR 코드가 생성되지 않았습니다."));

            String qrCodeUrl = qrCode.getQR_Code();

            String fullUrl = qrCodeUrl; // 수정 후: qrCodeUrl이 이미 완전한 URL임

            byte[] qrCodeBytes = qrCodeTestService.generateQrCodeBytes(fullUrl, 250, 250);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=qrcode.png")
                    .body(qrCodeBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "매장 QR코드 조회 (JSON)", description = "매장 ID로 저장된 QR 코드 정보를 JSON으로 반환합니다.")
    @GetMapping("/{storeId}/qrcode/json")
    public ResponseEntity<?> getStoreQrCodeJson(
            @Parameter(description = "매장 ID") @PathVariable Long storeId) {

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

            QR_Code qrCode = qrCodeRepository.findByStoreStoreId(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("QR 코드가 생성되지 않았습니다."));

            String qrCodeUrl = qrCode.getQR_Code(); // 주의: 엔티티 수정 전이라면 이 이름 사용
            String fullUrl = qrCodeUrl; // 수정 후: qrCodeUrl이 이미 완전한 URL임
            String qrCodeBase64 = qrCodeTestService.generateQrCodeBase64(fullUrl, 250, 250);

            Map<String, String> response = new HashMap<>();
            response.put("storeId", String.valueOf(storeId));
            response.put("qrCodeUrl", qrCodeUrl);
            response.put("fullUrl", fullUrl);
            response.put("qrCodeBase64", qrCodeBase64);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "QR 코드 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "가게 매출 정보 조회", description = "특정 가게의 일일/전체 총 매출 및 판매량을 조회합니다.")
    @GetMapping("/{storeId}/sales")
    public ResponseEntity<?> getStoreSales(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @AuthenticationPrincipal AppUser user) {

        // 권한 확인 로직 (Store 주인만 접근 가능하도록)
        if (user == null || user.getId() != storeId || !(user instanceof Store)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            StoreSalesResponseDto storeSales = storeService.getStoreSales(storeId);
            return ResponseEntity.ok(storeSales);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}


//TODO
/*
나중에 서버에 올릴때 URL 바꿔야함(지금은 로컬로 되게 하드코딩 되어 있음)
 */