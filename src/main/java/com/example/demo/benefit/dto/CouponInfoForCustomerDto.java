package com.example.demo.benefit.dto;

import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "고객에게 보여주기 위한 최소한의 쿠폰 정보")
public class CouponInfoForCustomerDto {
    /**
     * CouponInfoForCustomerDto 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 조건부 또는 선택
     *
     * couponName            ✅  쿠폰 이름
     * discountType          ✅  할인 방식 (정액/정률)
     * discountValue         ✅  할인 금액 또는 퍼센트
     * discountLimit         ⛔  정률 할인 시 최대 한도
     * minimumOrderAmount    ⛔  최소 주문 금액 조건
     * storeName             ✅  쿠폰 발급 상점 이름
     */

    @Schema(description = "쿠폰명", example = "가을맞이 10% 할인")
    private String couponName;

    @Schema(description = "할인 방식 (정액/정률)", example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(description = "할인 값 (금액 또는 퍼센트)", example = "10")
    private int discountValue;

    @Schema(description = "할인 한도 (정률 할인 시, 선택 사항)", example = "5000", nullable = true)
    private Integer discountLimit;

    @Schema(description = "최소 주문 금액 (선택 사항)", example = "10000", nullable = true)
    private Integer minimumOrderAmount;

    @Schema(description = "쿠폰이 속한 상점 이름", example = "메가커피")
    private String storeName;

    public static CouponInfoForCustomerDto fromEntity(Coupon coupon) {
        return CouponInfoForCustomerDto.builder()
                .couponName(coupon.getCouponName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .discountLimit(coupon.getDiscountLimit())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .storeName(coupon.getStore().getStoreName())
                .build();
    }
}

