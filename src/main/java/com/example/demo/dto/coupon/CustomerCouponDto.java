package com.example.demo.dto.coupon;

import com.example.demo.entity.customer.CustomerCoupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "고객 보유 쿠폰 정보 응답 DTO")
public class CustomerCouponDto {

    @Schema(description = "고객 쿠폰 ID (실제 사용/조회 시 이 ID를 사용)", example = "1")
    private Long customerCouponId;

    @Schema(description = "원본 쿠폰 정보")
    private CouponDto coupon;

    @Schema(description = "쿠폰 발급일시", example = "2023-10-27T10:00:00")
    private LocalDateTime issuedAt;

    @Schema(description = "쿠폰 만료일시", example = "2023-11-26T23:59:59")
    private LocalDateTime expiresAt;

    @Schema(description = "쿠폰 사용 여부", example = "false")
    private boolean isUsed;

    public static CustomerCouponDto fromEntity(CustomerCoupon customerCoupon) {
        return CustomerCouponDto.builder()
                .customerCouponId(customerCoupon.getCustomerCouponId())
                .coupon(CouponDto.fromEntity(customerCoupon.getCouponDetail().getCoupon()))
                .issuedAt(customerCoupon.getIssuedAt())
                .expiresAt(customerCoupon.getExpiresAt())
                .isUsed(customerCoupon.isUsed())
                .build();
    }
}
