package com.example.demo.benefit.service;

import com.example.demo.benefit.entity.CouponStatus;
import com.example.demo.benefit.repository.CouponRepository;
import com.example.demo.benefit.repository.CustomerCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponCleanupService {

    private final CustomerCouponRepository customerCouponRepository;
    private final CouponRepository couponRepository;

    /**
     * 매일 자정과 정오에 만료된 쿠폰들을 정리
     * 1. 만료된 CustomerCoupon 삭제 (expiresAt 기준)
     * 2. 사용된 CustomerCoupon 삭제 (USED 상태)
     * 3. 만료된 Coupon 엔티티 및 관련 CustomerCoupon 모두 삭제 (expiryDate 기준)
     */
    @Scheduled(cron = "${coupon.cleanup.cron}")
    @Transactional
    public void cleanupExpiredCoupons() {
        log.info("만료된 쿠폰 정리 작업을 시작합니다.");
        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. 만료된 CustomerCoupon 삭제 (expiresAt 기준)
            int expiredCustomerCoupons = customerCouponRepository.deleteByExpiresAtBefore(now);
            log.info("만료된 CustomerCoupon {} 개를 삭제했습니다.", expiredCustomerCoupons);

            // 2. 사용된 CustomerCoupon 삭제 (USED 상태)
            int usedCustomerCoupons = customerCouponRepository.deleteByCouponStatus(
                com.example.demo.benefit.entity.CouponStatus.USED
            );
            log.info("사용된 CustomerCoupon {} 개를 삭제했습니다.", usedCustomerCoupons);

            // 3. 만료된 Coupon 엔티티 및 관련 CustomerCoupon 모두 삭제
            int expiredCoupons = couponRepository.deleteByExpiryDateBefore(now);
            log.info("만료된 Coupon {} 개와 관련된 모든 CustomerCoupon을 삭제했습니다.", expiredCoupons);

            log.info("만료된 쿠폰 정리 작업이 성공적으로 완료되었습니다. " +
                    "CustomerCoupon(만료): {}, CustomerCoupon(사용됨): {}, Coupon(만료): {}",
                    expiredCustomerCoupons, usedCustomerCoupons, expiredCoupons);

        } catch (Exception e) {
            log.error("만료된 쿠폰 정리 중 오류가 발생했습니다.", e);
        }
    }
}
