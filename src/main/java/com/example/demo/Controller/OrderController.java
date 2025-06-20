package com.example.demo.Controller;

import com.example.demo.Service.CustomerOrderService;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.PointResponse;
import com.example.demo.entity.customer.Customer;
import com.example.demo.util.MemberValidUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "고객 주문 처리", description = "고객의 포인트 사용 주문 및 포인트 조회를 위한 API입니다.")
public class OrderController {
    private final CustomerOrderService customerOrderService;
    private final MemberValidUtil memberValidUtil;

//    @Operation(summary = "포인트로 주문하기", description = "고객이 포인트를 사용하여 메뉴를 주문합니다. 주문하려면 고객으로 인증되어야 합니다.")
//    @SecurityRequirement(name = "bearer-key")
//    @PostMapping("/order")
//    public ResponseEntity<Void> order(@RequestBody List<OrderDTO> orders, @AuthenticationPrincipal Customer customer, @RequestParam Long storeId) {
//        if (!memberValidUtil.isCustomer(customer)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
//        }
//        customerOrderService.order(orders, customer, storeId);
//        return ResponseEntity.ok().build();
//    }

//    @Operation(summary = "고객 포인트 조회", description = "현재 로그인된 고객의 보유 포인트를 조회합니다. 조회를 위해서는 고객으로 인증되어야 합니다.")
//    @SecurityRequirement(name = "bearer-key")
//    @PostMapping("/pointCheck")
//    public ResponseEntity<PointResponse> pointCheck(@AuthenticationPrincipal Customer customer) {
//
//        if (!memberValidUtil.isCustomer(customer)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        long point = customerOrderService.getPoint(customer);
//        return ResponseEntity.ok(new PointResponse(point));
//    }
}
