package com.example.demo.entity.coupon;

import com.example.demo.entity.store.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String couponUuid; // UUID 문자열 형태로 저장
//
//    @Column(name = "coupon_code", nullable = false)
//    private String couponCode; // DB에 존재하는 coupon_code 필드

    @Column(nullable = false)
    private String couponName; // 쿠폰명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // 할인방식 (정액/정률)

    @Column(nullable = false)
    private int discountValue; // 할인값

    private Integer discountLimit; // 할인한도 (정률할인시)

    private Integer minimumOrderAmount; // 최소주문금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpiryType expiryType; // 만료방식 (절대/상대)

    private LocalDateTime expiryDate; // 만료일 (절대만료시)
    private Integer expiryDays;       // 사용가능 기간(일) (상대만료시)

    private LocalDateTime issueStartTime; // 발급시작시간

    @Column(nullable = false)
    private int totalQuantity; // 총발급수량

    @Column(nullable = false)
    private int issuedQuantity = 0; // 현재발급수량

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "coupon_applicable_categories", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "category_name")
    private List<String> applicableCategories; // 사용가능카테고리 (비어있으면 전체 적용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 상점ID

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "coupon_detail_id", nullable = false)
    private CouponDetail couponDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status = CouponStatus.ACTIVE; // 상태 (활성/비활성/회수)

    @Version // 낙관적 잠금을 위한 버전 필드
    private Long version;

    @Builder
    public Coupon(String couponName, DiscountType discountType, int discountValue, Integer discountLimit,
                  Integer minimumOrderAmount, ExpiryType expiryType, LocalDateTime expiryDate, Integer expiryDays,
                  LocalDateTime issueStartTime, int totalQuantity, List<String> applicableCategories, Store store, CouponDetail couponDetail) {
//        this.couponUuid = UUID.randomUUID().toString();
//        this.couponCode = "CP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // 고유한 코드 생성
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.discountLimit = discountLimit;
        this.minimumOrderAmount = minimumOrderAmount;
        this.expiryType = expiryType;
        this.expiryDate = expiryDate;
        this.expiryDays = expiryDays;
        this.issueStartTime = issueStartTime;
        this.totalQuantity = totalQuantity;
        this.applicableCategories = applicableCategories;
        this.store = store;
        this.couponDetail = couponDetail;
    }

    public void issue() {
        if (this.issuedQuantity < this.totalQuantity) {
            this.issuedQuantity++;
        } else {
            throw new IllegalStateException("쿠폰 발급 수량이 초과되었습니다.");
        }
    }

    public CouponStatus getStatus() {
        return status;
    }
    public void changeStatus(CouponStatus status) {
        this.status = status;
    }

    public void updateCouponDetails(String couponName, DiscountType discountType, int discountValue, Integer discountLimit,
                                    Integer minimumOrderAmount, ExpiryType expiryType, LocalDateTime expiryDate, Integer expiryDays,
                                    LocalDateTime issueStartTime, int totalQuantity, List<String> applicableCategories) {
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.discountLimit = discountLimit;
        this.minimumOrderAmount = minimumOrderAmount;
        this.expiryType = expiryType;
        this.expiryDate = expiryDate;
        this.expiryDays = expiryDays;
        this.issueStartTime = issueStartTime;
        this.totalQuantity = totalQuantity;
        this.applicableCategories = applicableCategories;
    }
}