package com.example.demo.repository.coupon;

import com.example.demo.entity.coupon.Coupon;
import com.example.demo.entity.coupon.CouponStatus;
import com.example.demo.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 매장 ID로 쿠폰 조회 (한 매장에 여러 쿠폰이 있을 수 있으므로 List<Coupon> 반환 고려)
    List<Coupon> findByStore(Store store); // 반환 타입을 List<Coupon>으로 변경하거나, 단일 Coupon이 맞는지 확인 필요

    // 쿠폰 코드로 쿠폰 조회 (Coupon 엔티티에 couponCode 필드가 없으므로 주석 처리 또는 삭제)
    // Coupon findByCouponCode(String couponCode);

    // 쿠폰 상태로 쿠폰 조회
    List<Coupon> findByStatus(CouponStatus status);

    // 매장 ID와 쿠폰 ID로 쿠폰 조회
    Optional<Coupon> findByIdAndStore(Long couponId, Store store);


    // List<Coupon> findbyStore(Store store);
}