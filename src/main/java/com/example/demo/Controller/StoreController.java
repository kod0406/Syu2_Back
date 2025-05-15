package com.example.demo.Controller;

import com.example.demo.Service.StoreService;
import com.example.demo.dto.StoreLoginDTO;
import com.example.demo.dto.StoreRegistrationDTO;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.entity.store.Store;
import com.example.demo.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public StoreController(StoreService storeService, JwtTokenProvider jwtTokenProvider) {
        this.storeService = storeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStore(@RequestBody StoreRegistrationDTO registrationDTO) {
        try {
            Store store = storeService.registerStore(registrationDTO);
            return ResponseEntity.ok(Map.of(
                    "message", "매장 가입이 완료되었습니다.",
                    "storeId", store.getId(),
                    "email", store.getOwnerEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawStore(@AuthenticationPrincipal AppUser user,
                                           HttpServletResponse response) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            // 매장 삭제
            storeService.deleteStore(user.getId());

            // 쿠키 초기화
            Cookie cookie = new Cookie("access_token", null);
            cookie.setMaxAge(0); // 즉시 만료
            cookie.setPath("/");
            // cookie.setSecure(true); // HTTPS 환경에서만 활성화
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody StoreLoginDTO loginDTO,
                                   HttpServletResponse response) {
        try {
            // 실제 로그인 검증 로직 추가
            Store store = storeService.authenticateStore(loginDTO.getOwnerEmail(), loginDTO.getPassword());

            if (store == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "이메일 또는 비밀번호가 일치하지 않습니다."));
            }

            // JWT 토큰 생성
            String token = jwtTokenProvider.createToken(loginDTO.getOwnerEmail());

            // 쿠키에 토큰 저장
            Cookie cookie = new Cookie("access_token", token);
            cookie.setMaxAge(3600); // 1시간
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of(
                    "message", "로그인 성공",
                    "token", token,
                    "storeId", store.getStoreId()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // 로그 확인용
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인에 실패했습니다: " + e.getMessage()));
        }
    }
}