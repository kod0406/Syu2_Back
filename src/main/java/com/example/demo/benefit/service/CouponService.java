package com.example.demo.benefit.service;

import com.example.demo.benefit.dto.CouponCreateRequestDto;
import com.example.demo.benefit.dto.CouponDto;
import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.CouponStatus;
import com.example.demo.benefit.entity.ExpiryType;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.benefit.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.setting.exception.BusinessException;
import com.example.demo.setting.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {
    private final CouponRepository couponRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public CouponDto createCoupon(CouponCreateRequestDto requestDto, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        //만료됐는지 검사
        validateExpiryPolicy(requestDto);

        LocalDateTime issueStartTime = requestDto.getIssueStartTime();

        // 사용자가 발급 시작 시간을 '현재' 또는 '과거'로 지정한 경우,
        // DB와 서버 시간의 미세한 차이(race condition)로 인해 쿠폰이 즉시 조회되지 않는 문제를 해결합니다.
        // issueStartTime을 null로 설정하면, CouponRepository의 조회 쿼리에서
        // `c.issueStartTime IS NULL` 조건에 의해 '즉시 발급 가능한' 쿠폰으로 올바르게 인식됩니다.
        if (issueStartTime != null && !issueStartTime.isAfter(LocalDateTime.now())) {
            issueStartTime = null;
        }
        // 미래의 특정 시간으로 발급이 예약된 경우는, 해당 시간이 그대로 유지됩니다.

        Coupon coupon = Coupon.builder()
                .couponName(requestDto.getCouponName())
                .discountType(requestDto.getDiscountType())
                .discountValue(requestDto.getDiscountValue())
                .discountLimit(requestDto.getDiscountLimit())
                .minimumOrderAmount(requestDto.getMinimumOrderAmount())
                .expiryType(requestDto.getExpiryType())
                .expiryDate(requestDto.getExpiryDate())
                .expiryDays(requestDto.getExpiryDays())
                .issueStartTime(issueStartTime) // 조정된 값을 사용
                .totalQuantity(requestDto.getTotalQuantity())
                .applicableCategories(requestDto.getApplicableCategories())
                .store(store)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponDto.fromEntity(savedCoupon);
    }

    private void validateExpiryPolicy(CouponCreateRequestDto requestDto) {
        if (requestDto.getExpiryType() == ExpiryType.ABSOLUTE) {
            if (requestDto.getExpiryDate() == null) {
                throw new BusinessException(ErrorCode.INVALID_ABSOLUTE_EXPIRY_DATE);
            }
            if (requestDto.getExpiryDate().isBefore(requestDto.getIssueStartTime())) {
                throw new BusinessException(ErrorCode.EXPIRY_BEFORE_ISSUE);
            }
        } else if (requestDto.getExpiryType() == ExpiryType.RELATIVE) {
            if (requestDto.getExpiryDays() == null || requestDto.getExpiryDays() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_RELATIVE_EXPIRY_DAYS);
            }
            LocalDateTime expiryDate = requestDto.getIssueStartTime().plusDays(requestDto.getExpiryDays());
            if (expiryDate.isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.EXPIRY_DATE_IN_PAST);
            }
        } else {
            throw new BusinessException(ErrorCode.INVALID_EXPIRY_TYPE);
        }
    }

    @Transactional
    public CouponDto updateCoupon(Long couponId, Long storeId, CouponCreateRequestDto requestDto) { // 쿠폰 수정 시 사용할 DTO는 필요에 따라 CouponUpdateRequestDto 등으로 분리 가능
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store) // ID와 Store로 쿠폰 조회
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_DENIED));

        // 만료 정책 유효성 검사
        validateExpiryPolicy(requestDto);

        // Coupon 엔티티의 updateCouponDetails 메서드 호출
        coupon.updateCouponDetails(
                requestDto.getCouponName(),
                requestDto.getDiscountType(),
                requestDto.getDiscountValue(),
                requestDto.getDiscountLimit(),
                requestDto.getMinimumOrderAmount(),
                requestDto.getExpiryType(),
                requestDto.getExpiryDate(),
                requestDto.getExpiryDays(),
                requestDto.getIssueStartTime(),
                requestDto.getTotalQuantity(),
                requestDto.getApplicableCategories()
        );

        Coupon updatedCoupon = couponRepository.save(coupon);
        return CouponDto.fromEntity(updatedCoupon);
    }

    @Transactional
    public CouponDto updateCouponStatus(Long couponId, Long storeId, CouponStatus status) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store) // ID와 Store로 쿠폰 조회
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_DENIED));

        coupon.changeStatus(status);


        return CouponDto.fromEntity(coupon);
    }

    /**
     * 쿠폰을 삭제합니다. (ID 사용)
     *
     * @param couponId 삭제할 쿠폰의 ID
     * @param storeId  매장 ID
     * @return 삭제 성공 여부
     */
    @Transactional
    public boolean deleteCoupon(Long couponId, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_DENIED));

        // 발급된 쿠폰이 있는지 확인
        if (coupon.getIssuedQuantity() > 0) {
            throw new BusinessException(ErrorCode.CHANGE_DENIED);
        }

        couponRepository.delete(coupon);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getCouponsByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        List<Coupon> coupons = couponRepository.findAllByStore(store);
        return coupons.stream().map(CouponDto::fromEntity).toList();
    }
}
