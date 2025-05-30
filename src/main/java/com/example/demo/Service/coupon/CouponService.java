package com.example.demo.Service.coupon;

import com.example.demo.dto.coupon.CouponCreateRequestDto;
import com.example.demo.dto.coupon.CouponDto;
import com.example.demo.entity.coupon.Coupon;
import com.example.demo.entity.coupon.CouponStatus;
import com.example.demo.entity.coupon.ExpiryType;
import com.example.demo.entity.store.Store;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.coupon.CouponRepository;
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
    public CouponDto createCoupon(CouponCreateRequestDto requestDto, Long storeId){
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
                .store(store) // 현재 로그인한 상점주인의 상점과 연결
                .build();
        // 처음 쿠폰을 만들면 ACTIVE 상태로 설정됨

        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponDto.fromEntity(savedCoupon);
    }

    private void validateExpiryPolicy(CouponCreateRequestDto requestDto){
        if(requestDto.getExpiryType() == ExpiryType.ABSOLUTE){
            if(requestDto.getExpiryDate() == null){
                throw new IllegalArgumentException("절대 만료 방식을 선택한 경우 만료 날짜를 입력해야 합니다.");
            }
            if(requestDto.getExpiryDate().isBefore(requestDto.getIssueStartTime())){
                throw new IllegalArgumentException("만료 날짜는 발급 시작 시간 이후여야 합니다.");
            }
        }
        else if(requestDto.getExpiryType() == ExpiryType.RELATIVE){
            if(requestDto.getExpiryDays() == null || requestDto.getExpiryDays() <= 0){
                throw new IllegalArgumentException("상대 만료 방식을 선택한 경우 유효 기간(일)을 입력해야 합니다.");
            }
            LocalDateTime expiryDate = requestDto.getIssueStartTime().plusDays(requestDto.getExpiryDays());
            if(expiryDate.isBefore(LocalDateTime.now())){
                throw new IllegalArgumentException("상대 만료 방식의 만료 날짜는 현재 시간 이후여야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 만료 방식을 선택했습니다.");

        }
    }

    @Transactional
    public CouponDto updateCoupon(Long couponId, Long storeId, CouponCreateRequestDto requestDto){ // 쿠폰 수정 시 사용할 DTO는 필요에 따라 CouponUpdateRequestDto 등으로 분리 가능
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상점을 찾을 수 없습니다. ID: " + storeId));

        Coupon coupon = couponRepository.findByIdAndStore(couponId, store) // 두 번째 파라미터를 Store 객체로 수정
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
                requestDto.getApplicableCategories(),
                coupon.getStatus() // 현재 상태를 그대로 유지하거나, 필요시 DTO에 status 필드를 추가하여 변경
        );

        Coupon updatedCoupon = couponRepository.save(coupon);
        return CouponDto.fromEntity(updatedCoupon);
    }
}
