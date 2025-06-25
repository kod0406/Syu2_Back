package com.example.demo.benefit.repository;

import com.example.demo.benefit.entity.Coupon;
import com.example.demo.customer.entity.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, String> {
    Optional<CustomerCoupon> findByCustomerIdAndCouponId(Long customerId, Long couponId);
    List<CustomerCoupon> findByCustomerId(Long customerId);
    void deleteByExpiresAtBefore(LocalDateTime now);


    @Query("""
        SELECT c.coupon.couponName
        FROM CustomerCoupon c
        WHERE c.couponUuid = :uuid
        """)
    String findCouponNameByCouponUuid(@Param("uuid") String uuid);
}
