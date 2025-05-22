package com.example.demo.KakaoPay;

import com.example.demo.Service.CustomerOrderService;
import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.customer.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kakao-pay")
public class KakaoPayController {

    private final KakaoPayProvider kakaoPayProvider;
    private final CustomerOrderService customerOrderService;

//    @PostMapping("/ready")
//    public KakaoPayResponse.ReadyResponse ready(@RequestBody KakaoPayRequest.OrderRequest request){
//        return kakaoPayProvider.ready(request);
//    }

    @PostMapping("/ready")
    public KakaoPayResponse.ReadyResponse ready(@RequestBody List<OrderDTO> orders, @AuthenticationPrincipal Customer customer, @RequestParam Long storeId){

        KakaoPayRequest.OrderRequest request = customerOrderService.order(orders, customer, storeId);;
        return kakaoPayProvider.ready(request);
    }

    @GetMapping("/approve")
    public KakaoPayResponse.ApproveResponse approve(@RequestParam("pg_token") String pgToken) {
        //DB 저장 로직 추가
        return kakaoPayProvider.approve(pgToken);
    }
}