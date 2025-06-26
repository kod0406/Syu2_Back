package com.example.demo.user.controller;

import com.example.demo.setting.util.ApiResponse;
import com.example.demo.user.dto.MemberResponseDTO;
import com.example.demo.customer.entity.Customer;
import com.example.demo.store.entity.Store;
import com.example.demo.setting.util.MemberValidUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommonController {
    private final MemberValidUtil memberValidUtil;

    //고객용
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> getMe(@AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            return ResponseEntity.ok(ApiResponse.success("비회원입니다.", null));
        }

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", new MemberResponseDTO(customer)));
    }
    //가게 확인 로직
    @GetMapping("/auth/store")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> getStore(@AuthenticationPrincipal Store store) {
        memberValidUtil.validateIsStore(store);

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", new MemberResponseDTO(store)));
    }
}

