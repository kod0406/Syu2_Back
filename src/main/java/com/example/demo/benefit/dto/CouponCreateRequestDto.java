package com.example.demo.benefit.dto;

import com.example.demo.benefit.entity.DiscountType;
import com.example.demo.benefit.entity.ExpiryType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "쿠폰 생성 요청 DTO")
public class CouponCreateRequestDto {

    /**
     * CouponCreateRequestDto 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 조건부 또는 선택
     *
     * couponName          ✅  쿠폰 이름
     * discountType        ✅  할인 타입 (정액/정률)
     * discountValue       ✅  할인 금액 또는 퍼센트
     * expiryType          ✅  만료 타입 (절대/상대)
     * expiryDate          ⛔  expiryType == ABSOLUTE일 때만
     * expiryDays          ⛔  expiryType == RELATIVE일 때만
     * issueStartTime      ✅  발급 시작 시간 (미래 시점)
     * totalQuantity       ✅  총 발급 수량
     * applicableCategories⛔  특정 카테고리에만 적용할 경우 지정
     */


    @NotBlank(message = "쿠폰명을 입력해주세요.")
    @Schema(description = "쿠폰명", example = "신규 고객 환영 쿠폰")
    private String couponName;

    @NotNull(message = "할인 방식을 선택해주세요.")
    @Schema(description = "할인 방식 (PERCENTAGE: 정률 할인, FIXED_AMOUNT: 정액 할인)", example = "PERCENTAGE")
    private DiscountType discountType;

    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
    @Schema(description = "할인 값 (정액 할인 시 금액, 정률 할인 시 퍼센트)", example = "10")
    private int discountValue;

    @Schema(description = "정률 할인 시 최대 할인 금액 (선택 사항)", example = "5000", nullable = true)
    private Integer discountLimit;

    @Schema(description = "최소 주문 금액 (선택 사항, 이 금액 이상 주문 시 쿠폰 사용 가능)", example = "20000", nullable = true)
    private Integer minimumOrderAmount;

    @NotNull(message = "만료 방식을 선택해주세요.")
    @Schema(description = "만료 방식 (ABSOLUTE: 절대적 만료일, RELATIVE: 상대적 유효기간)", example = "ABSOLUTE")
    private ExpiryType expiryType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "절대 만료 시 만료 날짜 (expiryType이 ABSOLUTE일 경우 필요)", example = "2024-12-31T23:59:59", nullable = true)
    private LocalDateTime expiryDate;

    @Schema(description = "상대 만료 시 발급 후 유효 기간(일) (expiryType이 RELATIVE일 경우 필요)", example = "30", nullable = true)
    private Integer expiryDays;

    @NotNull(message = "발급 시작 시간을 입력해주세요.")
    @FutureOrPresent(message = "발급 시작 시간은 현재 이후여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "쿠폰 발급 시작 시간", example = "2023-10-26T10:00:00")
    private LocalDateTime issueStartTime;

    @Min(value = 1, message = "총 발급 수량은 1개 이상이어야 합니다.")
    @Schema(description = "총 발급 수량", example = "1000")
    private int totalQuantity;

    @Schema(description = "적용 가능 카테고리 목록 (비어있거나 null이면 전체 카테고리 적용)", example = "[\"음료\", \"디저트\"]", nullable = true)
    private List<String> applicableCategories;

}