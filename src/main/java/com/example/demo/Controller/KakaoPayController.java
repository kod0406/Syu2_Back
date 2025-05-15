package com.example.demo.Controller;

import com.example.demo.dto.KakaoPayProvider;
import com.example.demo.dto.KakaoPayRequest;
import com.example.demo.dto.KakaoPayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kakao-pay")
public class KakaoPayController {

    private final KakaoPayProvider kakaoPayProvider;

    @PostMapping("/ready")
    public KakaoPayResponse.ReadyResponse ready(@RequestBody KakaoPayRequest.OrderRequest request){
        return kakaoPayProvider.ready(request);
    }

    @GetMapping("/approve")
    public KakaoPayResponse.ApproveResponse approve(@RequestParam("pg_token") String pgToken) {
        return kakaoPayProvider.approve(pgToken);
    }
}