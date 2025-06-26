package com.example.demo.KakaoPay;

import lombok.*;

public class KakaoPayResponse {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ReadyResponse {

        String tid;
        String next_redirect_pc_url;
        String next_redirect_mobile_url;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ApproveResponse {

        String aid;                 // 요청 고유 번호
        String tid;                 // 결제 고유 번호
        String cid;                 // 가맹점 코드
        String partner_order_id;    // 가맹점 주문번호
        String partner_user_id;     // 가맹점 회원 id
        String payment_method_type; // 결제 수단, CARD 또는 MONEY 중 하나
        String item_name;           // 상품 이름
        String item_code;           // 상품 코드
        int quantity;               // 상품 수량
        String created_at;          // 결제 준비 요청 시각
        String approved_at;         // 결제 승인 시각
        String payload;             // 결제 승인 요청에 대해 저장한 값, 요청 시 전달된 내용
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedirectUrlResponse {
        private String redirectUrl;
    }
}
