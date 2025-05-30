package com.example.demo.repository.coupon;

import com.example.demo.entity.coupon.Coupon;
import com.example.demo.entity.coupon.CouponStatus;
import com.example.demo.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 매장 ID로 쿠폰 조회
    Coupon findByStore(Store store);

    // 쿠폰 코드로 쿠폰 조회
    Coupon findByCouponCode(String couponCode);

    // 쿠폰 상태로 쿠폰 조회
    List<Coupon> findByStatus(CouponStatus status);

    // 쿠폰 만료 여부로 쿠폰 조회
    List<Coupon> findbyStore(Store store);

    // 매장 ID와 쿠폰 ID로 쿠폰 조회
    Optional<Coupon> findByIdAndStore(Long couponId, Store store);
}
