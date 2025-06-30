package com.example.demo.benefit.repository;

import com.example.demo.benefit.entity.Coupon;
import com.example.demo.customer.entity.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, String> {
    Optional<CustomerCoupon> findByCustomerIdAndCouponId(Long customerId, Long couponId);
    List<CustomerCoupon> findByCustomerId(Long customerId);

    // 만료된 CustomerCoupon 삭제
    @Modifying
    @Query("DELETE FROM CustomerCoupon cc WHERE cc.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    // 사용된 CustomerCoupon 삭제
    @Modifying
    @Query("DELETE FROM CustomerCoupon cc WHERE cc.couponStatus = :status")
    int deleteByCouponStatus(@Param("status") com.example.demo.benefit.entity.CouponStatus status);

    // 특정 쿠폰을 발급받은 모든 고객 쿠폰 삭제 (상점 탈퇴 시 사용)
    @Modifying
    @Query("DELETE FROM CustomerCoupon cc WHERE cc.coupon = :coupon")
    int deleteByCoupon(@Param("coupon") Coupon coupon);

    @Query("""
        SELECT c.coupon.couponName
        FROM CustomerCoupon c
        WHERE c.couponUuid = :uuid
        """)
    String findCouponNameByCouponUuid(@Param("uuid") String uuid);
}
