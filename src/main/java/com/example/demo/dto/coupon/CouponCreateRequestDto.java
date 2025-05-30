package com.example.demo.dto.coupon;

import com.example.demo.entity.coupon.DiscountType;
import com.example.demo.entity.coupon.ExpiryType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CouponCreateRequestDto {

    @NotBlank(message = "쿠폰명을 입력해주세요.")
    private String couponName;

    @NotNull(message = "할인 방식을 선택해주세요.")
    private DiscountType discountType;

    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
    private int discountValue;

    private Integer discountLimit; // 정률 할인 시 최대 할인 금액

    private Integer minimumOrderAmount; // 최소 주문 금액

    @NotNull(message = "만료 방식을 선택해주세요.")
    private ExpiryType expiryType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiryDate; // 절대 만료 시 만료 날짜

    private Integer expiryDays; // 상대 만료 시 발급 후 유효 기간(일)

    @NotNull(message = "발급 시작 시간을 입력해주세요.")
    @FutureOrPresent(message = "발급 시작 시간은 현재 이후여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime issueStartTime;

    @Min(value = 1, message = "총 발급 수량은 1개 이상이어야 합니다.")
    private int totalQuantity;

    private List<String> applicableCategories; // 적용 가능 카테고리 (null이거나 비어있으면 전체)

}