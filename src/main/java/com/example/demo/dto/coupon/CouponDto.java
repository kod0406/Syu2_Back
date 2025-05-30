package com.example.demo.dto.coupon;

import com.example.demo.entity.coupon.*;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CouponDto {
    private Long id; // 쿠폰 ID
    private String couponName; // 쿠폰명
    private DiscountType discountType; // 할인방식 (정액/정률)
    private int discountValue; // 할인값
    private Integer discountLimit; // 할인한도 (정률할인시)
    private Integer minimumOrderAmount; // 최소주문금액
    private ExpiryType expiryType; // 만료방식 (절대/상대)
    private LocalDateTime expiryDate; // 만료일 (절대만료시)
    private Integer expiryDays; // 사용가능 기간(일) (상대만료시)
    private LocalDateTime issueStartTime; // 발급시작시간
    private int totalQuantity; // 총발급수량
    private int issuedQuantity; // 현재발급수량
    private List<String> applicableCategories; // 사용가능카테고리 (비어있으면 전체 적용)
    private Long storeId; // 상점ID
    private CouponStatus status; // 쿠폰 상태 (발급 가능, 발급 중, 발급 완료 등)

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
                .status(coupon.getStatus())
                .build();
    }
}
