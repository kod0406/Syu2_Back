package com.example.demo.Controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MemberResponseDTO;
import com.example.demo.entity.customer.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> getMe(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.ok(ApiResponse.success("비회원입니다.", null));
        }

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", new MemberResponseDTO(customer)));
    }
}
