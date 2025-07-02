package com.example.demo.benefit.service;

import com.example.demo.benefit.dto.CouponDto;
import com.example.demo.benefit.dto.CustomerCouponDto;
import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.CouponStatus;
import com.example.demo.benefit.entity.ExpiryType;
import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.entity.CustomerCoupon;
import com.example.demo.setting.exception.BusinessException;
import com.example.demo.setting.exception.ErrorCode;
import com.example.demo.store.entity.Store;
import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.benefit.repository.CouponRepository;
import com.example.demo.benefit.repository.CustomerCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerCouponService {

    private final CouponRepository couponRepository;
    private final CustomerRepository customerRepository;
    private final CustomerCouponRepository customerCouponRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public CustomerCouponDto issueCoupon(Long customerId, Long couponId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        return issueCouponInternal(customer, coupon);
    }

    private CustomerCouponDto issueCouponInternal(Customer customer, Coupon coupon) {
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.COUPON_NOT_ACTIVE);
        }

        if (coupon.getIssuedQuantity() >= coupon.getTotalQuantity()) {
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        }

        if (coupon.getIssueStartTime() != null && coupon.getIssueStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COUPON_NOT_YET_AVAILABLE);
        }

        // 중복 발급 체크
        customerCouponRepository.findByCustomerIdAndCouponId(customer.getId(), coupon.getId())
                .ifPresent(cc -> {
                    throw new BusinessException(ErrorCode.COUPON_DUPLICATE);
                });

        coupon.issue();

        LocalDateTime expiresAt;
        if (coupon.getExpiryType() == ExpiryType.ABSOLUTE) {
            expiresAt = coupon.getExpiryDate();
        } else { // RELATIVE
            expiresAt = LocalDateTime.now().plusDays(coupon.getExpiryDays());
        }

        CustomerCoupon customerCoupon = CustomerCoupon.builder()
                .couponUuid(UUID.randomUUID().toString())
                .customer(customer)
                .coupon(coupon)
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .couponStatus(CouponStatus.UNUSED)
                .build();

        CustomerCoupon savedCustomerCoupon = customerCouponRepository.save(customerCoupon);
        return CustomerCouponDto.fromEntity(savedCustomerCoupon);
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getAvailableCoupons(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        List<Coupon> availableCoupons = couponRepository.findAvailableCouponsByStore(store);
        return availableCoupons.stream()
                .map(CouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getAllAvailableCoupons() {
        List<Coupon> availableCoupons = couponRepository.findAllAvailableCoupons(LocalDateTime.now().plusHours(9));
        return availableCoupons.stream()
                .map(CouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerCouponDto> getMyCoupons(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        List<CustomerCoupon> myCoupons = customerCouponRepository.findByCustomerId(customerId);
        return myCoupons.stream()
                .filter(customerCoupon -> customerCoupon.getCoupon().getStatus() == CouponStatus.ACTIVE)
                // ✅ 만료된 CustomerCoupon 필터링 추가
                .filter(customerCoupon -> customerCoupon.getExpiresAt().isAfter(LocalDateTime.now()))
                // ✅ 부모 쿠폰의 절대 만료 시간 검증 추가
                .filter(customerCoupon -> {
                    LocalDateTime couponExpiryDate = customerCoupon.getCoupon().getExpiryDate();
                    return couponExpiryDate == null || couponExpiryDate.isAfter(LocalDateTime.now());
                })
                .map(CustomerCouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerCouponDto> getMyUsableCouponsInStore(Long customerId, Long storeId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        if (!storeRepository.existsById(storeId)) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        List<CustomerCoupon> myCoupons = customerCouponRepository.findByCustomerId(customerId);

        return myCoupons.stream()
                .filter(cc -> cc.getCoupon().getStore().getId().equals(storeId))
                .filter(cc -> cc.getCoupon().getStatus() == CouponStatus.ACTIVE)
                .filter(cc -> cc.getCouponStatus() == CouponStatus.UNUSED)
                .filter(cc -> cc.getExpiresAt().isAfter(LocalDateTime.now()))
                // ✅ 부모 쿠폰의 절대 만료 시간 검증 추가
                .filter(cc -> {
                    LocalDateTime couponExpiryDate = cc.getCoupon().getExpiryDate();
                    return couponExpiryDate == null || couponExpiryDate.isAfter(LocalDateTime.now());
                })
                .map(CustomerCouponDto::fromEntity)
                .collect(Collectors.toList());
    }
}