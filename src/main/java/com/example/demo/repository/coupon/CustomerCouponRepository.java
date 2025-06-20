package com.example.demo.repository.coupon;

import com.example.demo.entity.customer.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, String> {
    Optional<CustomerCoupon> findByCustomerIdAndCouponId(Long customerId, Long couponId);
    List<CustomerCoupon> findByCustomerId(Long customerId);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
