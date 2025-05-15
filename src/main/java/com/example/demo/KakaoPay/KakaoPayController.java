package com.example.demo.KakaoPay;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kakao-pay")
@Tag(name = "카카오페이", description = "카카오페이 결제 관련 API")
public class KakaoPayController {

    private final KakaoPayProvider kakaoPayProvider;

    @Operation(summary = "결제 준비", description = "카카오페이 결제 준비 요청을 합니다.")
    @PostMapping("/ready")
    public KakaoPayResponse.ReadyResponse ready(
            @Parameter(description = "결제 요청 정보") @RequestBody KakaoPayRequest.OrderRequest request){
        return kakaoPayProvider.ready(request);
    }

    @Operation(summary = "결제 승인", description = "카카오페이 결제 승인 요청을 합니다.")
    @GetMapping("/approve")
    public KakaoPayResponse.ApproveResponse approve(
            @Parameter(description = "결제 승인 토큰") @RequestParam("pg_token") String pgToken) {
        return kakaoPayProvider.approve(pgToken);
    }
}
