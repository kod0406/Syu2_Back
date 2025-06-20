package com.example.demo.Service.coupon;

import com.example.demo.dto.coupon.CouponDto;
import com.example.demo.dto.coupon.CustomerCouponDto;
import com.example.demo.entity.coupon.Coupon;
import com.example.demo.entity.coupon.CouponStatus;
import com.example.demo.entity.coupon.ExpiryType;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.customer.CustomerCoupon;
import com.example.demo.entity.store.Store;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.coupon.CouponRepository;
import com.example.demo.repository.coupon.CustomerCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerCouponService {

    private final CouponRepository couponRepository;
    private final CustomerRepository customerRepository;
    private final CustomerCouponRepository customerCouponRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void issueCoupon(Long customerId, Long couponId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        issueCouponInternal(customer, coupon);
    }

    @Transactional
    public void issueCouponByUuid(Long customerId, String couponUuid) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        Coupon coupon = couponRepository.findByCouponDetail_CouponUuidWithPessimisticLock(couponUuid)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        // 중복 발급 체크: couponDetail의 couponUuid로 체크
        customerCouponRepository.findByCustomerIdAndCouponDetail_CouponUuid(customer.getId(), coupon.getCouponDetail().getCouponUuid())
                .ifPresent(cc -> {
                    throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
                });

        issueCouponInternal(customer, coupon);
    }

    private void issueCouponInternal(Customer customer, Coupon coupon) {
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalStateException("현재 발급 가능한 쿠폰이 아닙니다.");
        }

        if (coupon.getIssuedQuantity() >= coupon.getTotalQuantity()) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }

        if (coupon.getIssueStartTime() != null && coupon.getIssueStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("아직 쿠폰을 발급받을 수 없습니다.");
        }

        // 중복 발급 체크: couponDetail의 couponUuid로 체크
        customerCouponRepository.findByCustomerIdAndCouponDetail_CouponUuid(customer.getId(), coupon.getCouponDetail().getCouponUuid())
                .ifPresent(cc -> {
                    throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
                });

        coupon.issue();

        LocalDateTime expiresAt;
        if (coupon.getExpiryType() == ExpiryType.ABSOLUTE) {
            expiresAt = coupon.getExpiryDate();
        } else { // RELATIVE
            expiresAt = LocalDateTime.now().plusDays(coupon.getExpiryDays());
        }

        CustomerCoupon customerCoupon = CustomerCoupon.builder()
                .customer(customer)
                .couponDetail(coupon.getCouponDetail())
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        customerCouponRepository.save(customerCoupon);
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getAvailableCoupons(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));
        List<Coupon> availableCoupons = couponRepository.findAvailableCouponsByStore(store);
        return availableCoupons.stream()
                .map(CouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getAllAvailableCoupons() {
        List<Coupon> availableCoupons = couponRepository.findAllAvailableCoupons();
        return availableCoupons.stream()
                .map(CouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerCouponDto> getMyCoupons(Long customerId) {
        if (!customerRepository.findById(customerId).isPresent()) {
            throw new IllegalArgumentException("고객을 찾을 수 없습니다.");
        }
        List<CustomerCoupon> myCoupons = customerCouponRepository.findByCustomerId(customerId);
        return myCoupons.stream()
                .map(CustomerCouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponDto getCouponByUuid(String couponUuid) {
        Coupon coupon = couponRepository.findByCouponDetail_CouponUuid(couponUuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 UUID의 쿠폰을 찾을 수 없습니다."));
        return CouponDto.fromEntity(coupon);
    }
}