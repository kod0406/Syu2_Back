package com.example.demo.repository.coupon;

import com.example.demo.entity.customer.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Long> {
    Optional<CustomerCoupon> findByCustomerIdAndCouponDetail_CouponUuid(Long customerId, String couponUuid);
    List<CustomerCoupon> findByCustomerId(Long customerId);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
