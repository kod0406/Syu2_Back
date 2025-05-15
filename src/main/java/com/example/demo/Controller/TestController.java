package com.example.demo.Controller;

import com.example.demo.entity.entityInterface.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
@Tag(name = "테스트", description = "테스트용 API")
public class TestController {
    
    @Operation(summary = "사용자 정보 조회", description = "현재 인증된 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "role", user.getRole(),
                "id", user.getId()
        ));
    }
}
