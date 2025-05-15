package com.example.demo.KakaoPay;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class KakaoPayProvider {
    @Value("${kakaopay.secretKey}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;
    //tid값 클라이언트에서 받아오게 수정
    private String tid;

    public KakaoPayResponse.ReadyResponse ready(KakaoPayRequest.OrderRequest request) {

        Map<String, String> parameters = new HashMap<>();

        parameters.put("cid", cid); // 가맹점 코드, 테스트용은 TC0ONETIME
        parameters.put("partner_order_id", "1234567890"); // 주문번호, 임시 : 1234567890
        parameters.put("partner_user_id", "1234567890"); // 회원아이디, 임시 : 1234567890
        parameters.put("item_name", request.getItemName()); // 상품명
        parameters.put("quantity", request.getQuantity()); // 상품 수량
        parameters.put("total_amount", request.getTotalPrice()); // 상품 총액
        parameters.put("tax_free_amount", "0"); // 상품 비과세 금액
        parameters.put("approval_url", "http://localhost:8080/api/v1/kakao-pay/approve"); // 결제 성공 시 redirct URL
        parameters.put("cancel_url", "http://localhost:8080/api/v1/kakao-pay/cancel"); // 결제 취소 시
        parameters.put("fail_url", "http://localhost:8080/kakao-pay/fail"); // 결제 실패 시

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        ResponseEntity<KakaoPayResponse.ReadyResponse> response = restTemplate.postForEntity(url, entity, KakaoPayResponse.ReadyResponse.class);

        tid = Objects.requireNonNull(response.getBody()).getTid();
        log.info("로그: " + response.getBody().getTid());
        return response.getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + secretKey);
        headers.add("Content-type", "application/json");
        return headers;
    }

    public KakaoPayResponse.ApproveResponse approve(String pgToken) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", cid);
        parameters.put("tid", tid);
        parameters.put("partner_order_id", "1234567890");
        parameters.put("partner_user_id", "1234567890");
        parameters.put("pg_token", pgToken); // 결제승인 요청을 인증하는 토큰

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        ResponseEntity<KakaoPayResponse.ApproveResponse> response = restTemplate.postForEntity(url, entity, KakaoPayResponse.ApproveResponse.class);

        return response.getBody();
    }

}
