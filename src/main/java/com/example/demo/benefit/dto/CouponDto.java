package com.example.demo.benefit.dto;

import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.CouponStatus;
import com.example.demo.benefit.entity.DiscountType;
import com.example.demo.benefit.entity.ExpiryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "쿠폰 정보 응답 DTO")
public class CouponDto {

    /**
     * CouponDto 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 조건부 또는 선택
     *
     * id                    ✅  쿠폰 ID
     * couponName            ✅  쿠폰 이름
     * discountType          ✅  할인 방식 (정액/정률)
     * discountValue         ✅  할인 금액 또는 퍼센트
     * discountLimit         ⛔  정률 할인 시 최대 한도
     * minimumOrderAmount    ⛔  최소 주문 금액 조건
     * expiryType            ✅  만료 방식 (절대/상대)
     * expiryDate            ⛔  expiryType == ABSOLUTE일 때
     * expiryDays            ⛔  expiryType == RELATIVE일 때
     * issueStartTime        ✅  발급 시작 시간
     * totalQuantity         ✅  총 발급 수량
     * issuedQuantity        ✅  현재 발급된 수량
     * applicableCategories  ⛔  특정 카테고리 한정 적용
     * storeId               ✅  소속 상점 ID
     * storeName             ✅  소속 상점 이름
     * status                ✅  쿠폰 상태 (예: ACTIVE)
     */

    @Schema(description = "쿠폰 ID", example = "1")
    private Long id;

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

    @Schema(description = "만료 방식 (절대/상대)", example = "ABSOLUTE")
    private ExpiryType expiryType;

    @Schema(description = "만료일 (절대 만료 시)", example = "2024-11-30T23:59:59", nullable = true)
    private LocalDateTime expiryDate;

    @Schema(description = "사용 가능 기간(일) (상대 만료 시)", example = "30", nullable = true)
    private Integer expiryDays;

    @Schema(description = "발급 시작 시간", example = "2023-10-01T00:00:00")
    private LocalDateTime issueStartTime;

    @Schema(description = "총 발급 수량", example = "1000")
    private int totalQuantity;

    @Schema(description = "현재 발급된 수량", example = "150")
    private int issuedQuantity;

    @Schema(description = "사용 가능 카테고리 목록 (비어있으면 전체 적용)", example = "[\"커피\", \"베이커리\"]", nullable = true)
    private List<String> applicableCategories;

    @Schema(description = "쿠폰이 속한 상점 ID", example = "101")
    private Long storeId;

    @Schema(description = "쿠폰이 속한 상점 이름", example = "메가커피")
    private String storeName;

    @Schema(description = "쿠폰 상태 (발급 가능, 발급 중, 발급 완료 등)", example = "ACTIVE")
    private CouponStatus status;

    public static CouponDto fromEntity(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .couponName(coupon.getCouponName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .discountLimit(coupon.getDiscountLimit())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .expiryType(coupon.getExpiryType())
                .expiryDate(coupon.getExpiryDate())
                .expiryDays(coupon.getExpiryDays())
                .issueStartTime(coupon.getIssueStartTime())
                .totalQuantity(coupon.getTotalQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .applicableCategories(coupon.getApplicableCategories())
                .storeId(coupon.getStore().getId())
                .storeName(coupon.getStore().getStoreName())
                .status(coupon.getStatus())
                .build();
    }
}