package com.example.demo.Controller;

import com.example.demo.Service.StoreService;
import com.example.demo.dto.StoreLoginDTO;
import com.example.demo.dto.StoreRegistrationDTO;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.entity.store.Store;
import com.example.demo.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "매장 관리", description = "매장 계정 관리 API")
public class StoreController {
    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public StoreController(StoreService storeService, JwtTokenProvider jwtTokenProvider) {
        this.storeService = storeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "매장 회원가입", description = "신규 매장을 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<?> registerStore(
            @Parameter(description = "매장 등록 정보") @RequestBody StoreRegistrationDTO registrationDTO) {
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
}

   