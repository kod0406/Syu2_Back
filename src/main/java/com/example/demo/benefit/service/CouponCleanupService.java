package com.example.demo.benefit.service;

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

    /**
     * 매시간 만료된 쿠폰을 삭제
     */
    @Scheduled(cron = "0 0 0,12 * * ?")
    @Transactional
    public void cleanupExpiredCoupons() {
        log.info("만료된 쿠폰 정리 작업을 시작합니다.");
        try {
            LocalDateTime now = LocalDateTime.now();
            customerCouponRepository.deleteByExpiresAtBefore(now);
            log.info("만료된 쿠폰 정리 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("만료된 쿠폰 정리 중 오류가 발생했습니다.", e);
        }
    }
}
