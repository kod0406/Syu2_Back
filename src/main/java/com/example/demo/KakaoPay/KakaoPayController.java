package com.example.demo.KakaoPay;

import com.example.demo.customer.service.CustomerOrderService;
import com.example.demo.order.dto.OrderDTO;
import com.example.demo.customer.entity.Customer;
import com.example.demo.order.dto.OrderGroupBatchMessage;
import com.example.demo.setting.util.MemberValidUtil;
import com.example.demo.setting.webSock.WebBroadCast;
import com.example.demo.store.entity.Store;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/kakao-pay")
@Tag(name = "카카오페이 결제", description = "카카오페이를 이용한 결제 프로세스를 처리하는 API입니다.")
public class KakaoPayController {

    private final KakaoPayProvider kakaoPayProvider;
    private final CustomerOrderService customerOrderService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PostMapping("/ready")
    public KakaoPayResponse.RedirectUrlResponse ready(
            @RequestBody List<OrderDTO> orders,
            @AuthenticationPrincipal Customer customer,
            @RequestParam Long storeId,
            @RequestHeader("User-Agent") String userAgent) {

        log.info("Kakao Pay ready : {}, UA: {}", customer, userAgent);
        KakaoPayRequest.OrderRequest request = customerOrderService.order(orders, customer, storeId);
        return kakaoPayProvider.ready(request, userAgent); // ✅ redirectUrl만 담긴 응답
    }

    @Operation(summary = "카카오페이 결제 승인", description = "사용자가 카카오페이 결제를 성공적으로 완료한 후, 카카오로부터 리다이렉트되는 엔드포인트입니다. 결제 승인 처리를 진행하고, 성공 시 지정된 페이지로 리다이렉트됩니다.")
    @GetMapping("/approve")
    public ResponseEntity<Void> approve(@RequestParam("pg_token") String pgToken,
                                        @RequestParam("orderGroupId") Long orderGroupId) {
        //DB 저장 로직 추가
        kakaoPayProvider.approve(pgToken, orderGroupId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(frontendUrl + "/index")); // ✅ 결제 완료 후 이동할 URL (React 메인 페이지 등)

        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 Redirect

    }

    @Operation(summary = "카카오페이 결제 실패", description = "카카오페이 결제가 실패했을 때 호출되는 엔드포인트입니다. 결제 실패와 관련된 주문 데이터를 처리합니다.")
    @GetMapping("/fail")
    public ResponseEntity<String> fail(@RequestParam("orderGroupId") Long orderGroupId) {
        customerOrderService.processPaymentFailure(orderGroupId);
        return ResponseEntity.ok("결제 실패로 주문 삭제 완료");
    }


}