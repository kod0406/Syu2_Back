package com.example.demo.dto.coupon;

import com.example.demo.entity.coupon.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
public class CouponStatusUpdateRequestDto {

    @NotNull(message = "쿠폰 상태는 필수입니다.")
    @Schema(description = "변경할 쿠폰 상태 (ACTIVE, INACTIVE, RECALLED 중 하나)", example = "INACTIVE", required = true)
    private CouponStatus status;
}
