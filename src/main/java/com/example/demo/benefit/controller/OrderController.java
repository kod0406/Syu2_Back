package com.example.demo.benefit.controller;

import com.example.demo.customer.service.CustomerOrderService;
import com.example.demo.benefit.dto.PointResponse;
import com.example.demo.customer.entity.Customer;
import com.example.demo.setting.exception.UnauthorizedException;
import com.example.demo.setting.util.MemberValidUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "고객 주문 처리", description = "고객의 포인트 사용 주문 및 포인트 조회를 위한 API입니다.")
public class OrderController {
    private final CustomerOrderService customerOrderService;
    private final MemberValidUtil memberValidUtil;

    @Operation(summary = "고객 포인트 조회", description = "현재 로그인된 고객의 보유 포인트를 조회합니다. 조회를 위해서는 고객으로 인증되어야 합니다.")
    @SecurityRequirement(name = "bearer-key")
    @PostMapping("/api/pointCheck")
    public ResponseEntity<PointResponse> pointCheck(@AuthenticationPrincipal Customer customer) {

        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        long point = customerOrderService.getPoint(customer);
        return ResponseEntity.ok(new PointResponse(point));
    }
}
