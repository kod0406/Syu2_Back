package com.example.demo.benefit.dto;

import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.CouponStatus;
import com.example.demo.benefit.entity.DiscountType;
import com.example.demo.customer.entity.CustomerCoupon;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "고객 보유 쿠폰 정보 응답 DTO")
public class CustomerCouponDto {

    @Schema(description = "고객 쿠폰의 고유 ID (UUID)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String id;

    @Schema(description = "원본 쿠폰의 ID", example = "1")
    private Long couponId;

    @Schema(description = "쿠폰명", example = "가을맞이 10% 할인")
    private String couponName;

    @Schema(description = "할인 방식", example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(description = "할인 값", example = "10")
    private int discountValue;

    @Schema(description = "최소 주문 금액", example = "10000")
    private Integer minimumOrderAmount;

    @Schema(description = "할인 한도 (정률 할인 시)", example = "5000", nullable = true)
    private Integer discountLimit;

    @Schema(description = "사용 가능 카테고리 목록 (비어있으면 전체 적용)", example = "[\"커피\", \"베이커리\"]", nullable = true)
    private List<String> applicableCategories;

    @Schema(description = "쿠폰 발급일시", example = "2023-10-27T10:00:00")
    private LocalDateTime issuedAt;

    @Schema(description = "쿠폰 만료일시", example = "2023-11-26T23:59:59")
    private LocalDateTime expiresAt;

    @Schema(description = "쿠폰 사용 여부", example = "false")
    @JsonProperty("isUsed") // JSON으로 변환 시 필드명을 "isUsed"로 명시
    private boolean isUsed;

    @Schema(description = "쿠폰을 발급한 상점 이름", example = "메가커피")
    private String storeName;

    public static CustomerCouponDto fromEntity(CustomerCoupon customerCoupon) {
        Coupon coupon = customerCoupon.getCoupon();
        return CustomerCouponDto.builder()
                .id(customerCoupon.getCouponUuid()) // CustomerCoupon의 PK는 UUID
                .couponId(coupon.getId()) // 원본 Coupon의 PK
                .couponName(coupon.getCouponName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .discountLimit(coupon.getDiscountLimit())
                .applicableCategories(coupon.getApplicableCategories())
                .issuedAt(customerCoupon.getIssuedAt())
                .expiresAt(customerCoupon.getExpiresAt())
                .isUsed(customerCoupon.getCouponStatus() == CouponStatus.USED)
                .storeName(coupon.getStore().getStoreName())
                .build();
    }
}