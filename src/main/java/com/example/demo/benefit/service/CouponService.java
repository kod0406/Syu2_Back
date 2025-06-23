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
                .orElseThrow(() -> new IllegalArgumentException("해당 상점을 찾을 수 없습니다."));

        //만료됐는지 검사
        validateExpiryPolicy(requestDto);

        Coupon coupon = Coupon.builder()
                .couponName(requestDto.getCouponName())
                .discountType(requestDto.getDiscountType())
                .discountValue(requestDto.getDiscountValue())
                .discountLimit(requestDto.getDiscountLimit())
                .minimumOrderAmount(requestDto.getMinimumOrderAmount())
                .expiryType(requestDto.getExpiryType())
                .expiryDate(requestDto.getExpiryDate())
                .expiryDays(requestDto.getExpiryDays())
                .issueStartTime(requestDto.getIssueStartTime())
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
                throw new IllegalArgumentException("절대 만료 방식을 선택한 경우 만료 날짜를 입력해야 합니다.");
            }
            if (requestDto.getExpiryDate().isBefore(requestDto.getIssueStartTime())) {
                throw new IllegalArgumentException("만료 날짜는 발급 시작 시간 이후여야 합니다.");
            }
        } else if (requestDto.getExpiryType() == ExpiryType.RELATIVE) {
            if (requestDto.getExpiryDays() == null || requestDto.getExpiryDays() <= 0) {
                throw new IllegalArgumentException("상대 만료 방식을 선택한 경우 유효 기간(일)을 입력해야 합니다.");
            }
            LocalDateTime expiryDate = requestDto.getIssueStartTime().plusDays(requestDto.getExpiryDays());
            if (expiryDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("상대 만료 방식의 만료 날짜는 현재 시간 이후여야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 만료 방식을 선택했습니다.");

        }
    }

    @Transactional
    public CouponDto updateCoupon(Long couponId, Long storeId, CouponCreateRequestDto requestDto) { // 쿠폰 수정 시 사용할 DTO는 필요에 따라 CouponUpdateRequestDto 등으로 분리 가능
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상점을 찾을 수 없습니다. ID: " + storeId));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store) // ID와 Store로 쿠폰 조회
                .orElseThrow(() -> new IllegalArgumentException("해당 상점에 존재하지 않는 쿠폰이거나 수정 권한이 없습니다. 쿠폰 ID: " + couponId));

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
                .orElseThrow(() -> new IllegalArgumentException("해당 상점을 찾을 수 없습니다. ID: " + storeId));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store) // ID와 Store로 쿠폰 조회
                .orElseThrow(() -> new IllegalArgumentException("해당 상점에 존재하지 않는 쿠폰이거나 수정 권한이 없습니다. 쿠폰 ID: " + couponId));

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
                .orElseThrow(() -> new IllegalArgumentException("해당 상점을 찾을 수 없습니다. ID: " + storeId));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store)
                .orElseThrow(() -> new IllegalArgumentException("해당 상점에 존재하지 않는 쿠폰이거나 삭제 권한이 없습니다. 쿠폰 ID: " + couponId));

        // 발급된 쿠폰이 있는지 확인
        if (coupon.getIssuedQuantity() > 0) {
            throw new IllegalStateException("이미 고객에게 발급된 쿠폰은 삭제할 수 없습니다. 대신 상태를 변경하세요.");
        }

        couponRepository.delete(coupon);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getCouponsByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상점을 찾을 수 없습니다. ID: " + storeId));
        List<Coupon> coupons = couponRepository.findAllByStore(store);
        return coupons.stream().map(CouponDto::fromEntity).toList();
    }
}
