package com.example.demo.dto.coupon;

import com.example.demo.entity.coupon.CouponDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "쿠폰 상세 정보 DTO")
public class CouponDetailDto {
    @Schema(description = "쿠폰 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String couponUuid;

    @Schema(description = "쿠폰 코드", example = "CP12345678")
    private String couponCode;

    public static CouponDetailDto fromEntity(CouponDetail detail) {
        if (detail == null) return null;
        return CouponDetailDto.builder()
                .couponUuid(detail.getCouponUuid())
                .couponCode(detail.getCouponCode())
                .build();
    }
}
