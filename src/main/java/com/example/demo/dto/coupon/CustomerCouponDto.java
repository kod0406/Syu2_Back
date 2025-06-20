package com.example.demo.dto.coupon;

import com.example.demo.entity.coupon.CouponStatus;
import com.example.demo.entity.customer.CustomerCoupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "고객 보유 쿠폰 정보 응답 DTO")
public class CustomerCouponDto {

    @Schema(description = "고객 쿠폰 고유 ID (UUID), 발급시 각기 다른 값이 들어감. ", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String couponUuid;

    @Schema(description = "쿠폰 정보(쿠폰명, 할인방식, 할인값, 할인한도, 최소주문 금액, 상점 이름 정보만 제공)")
    private CouponInfoForCustomerDto coupon;

    @Schema(description = "쿠폰 발급일시", example = "2023-10-27T10:00:00")
    private LocalDateTime issuedAt;

    @Schema(description = "쿠폰 만료일시", example = "2023-11-26T23:59:59")
    private LocalDateTime expiresAt;

    @Schema(description = "쿠폰 사용 상태", example = "UNUSED")
    private CouponStatus couponStatus;

    public static CustomerCouponDto fromEntity(CustomerCoupon customerCoupon) {
        return CustomerCouponDto.builder()
                .couponUuid(customerCoupon.getCouponUuid())
                .coupon(CouponInfoForCustomerDto.fromEntity(customerCoupon.getCoupon()))
                .issuedAt(customerCoupon.getIssuedAt())
                .expiresAt(customerCoupon.getExpiresAt())
                .couponStatus(customerCoupon.getCouponStatus())
                .build();
    }
}
