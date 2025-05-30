package com.example.demo.Controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MemberResponseDTO;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {
    //고객용
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> getMe(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("비회원입니다.", null));
        }

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", new MemberResponseDTO(user)));
    }
    //가게 확인 로직
    @GetMapping("/auth/store")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> getStore(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("비회원입니다.", null));
        }

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", new MemberResponseDTO(user)));
    }
}
