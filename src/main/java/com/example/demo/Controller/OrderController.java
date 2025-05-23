package com.example.demo.Controller;

import com.example.demo.Service.CustomerOrderService;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.PointResponse;
import com.example.demo.entity.customer.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final CustomerOrderService customerOrderService;
    @PostMapping("/order")
    public ResponseEntity<Void> order(@RequestBody List<OrderDTO> orders, @AuthenticationPrincipal Customer customer, @RequestParam Long storeId) {
        customerOrderService.order(orders, customer, storeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pointCheck")
    public ResponseEntity<PointResponse> pointCheck(@AuthenticationPrincipal Customer customer) {

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인 안 한 경우
        }

        long point = customerOrderService.getPoint(customer);
        return ResponseEntity.ok(new PointResponse(point));
    }
}
