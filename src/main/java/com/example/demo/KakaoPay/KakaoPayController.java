package com.example.demo.KakaoPay;

import com.example.demo.Service.CustomerOrderService;
import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.customer.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
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

        //저장 로직 변경 결제 후에 저장해야함. 현재는 결제 전에 저장
        log.info("Kakao Pay ready : " + customer);
        KakaoPayRequest.OrderRequest request = customerOrderService.order(orders, customer, storeId);;
        return kakaoPayProvider.ready(request);
    }

    @GetMapping("/approve")
    public ResponseEntity<Void> approve(@RequestParam("pg_token") String pgToken,
                                        @RequestParam("orderGroupId") Long orderGroupId) {
        //DB 저장 로직 추가
        kakaoPayProvider.approve(pgToken, orderGroupId);
        //KakaoPayRequest.OrderRequest request = customerOrderService.order(orders, customer, storeId);;
        HttpHeaders headers = new HttpHeaders();
        //웹 소켓 로직 추가










        //TODO
        headers.setLocation(URI.create("http://localhost:3000/menu")); // ✅ 결제 완료 후 이동할 URL (React 메인 페이지 등)

        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 Redirect

    }

    @GetMapping("/fail")
    public ResponseEntity<String> fail(@RequestParam("orderGroupId") Long orderGroupId) {
        customerOrderService.processPaymentFailure(orderGroupId);
        return ResponseEntity.ok("결제 실패로 주문 삭제 완료");
    }

}