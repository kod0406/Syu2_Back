package com.example.demo.KakaoPay;

import com.example.demo.order.entity.OrderGroup;
import lombok.*;

public class KakaoPayRequest {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OrderRequest {

        String itemName;
        String quantity;
        String totalPrice;
        OrderGroup OrderGroup;
        boolean active;
    }
}