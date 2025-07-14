package com.example.demo;

import com.example.demo.benefit.dto.CustomerCouponDto;
import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.entity.CouponStatus;
import com.example.demo.benefit.entity.DiscountType;
import com.example.demo.benefit.entity.ExpiryType;
import com.example.demo.benefit.repository.CouponRepository;
import com.example.demo.benefit.repository.CustomerCouponRepository;
import com.example.demo.benefit.service.CustomerCouponService;
import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.entity.CustomerCoupon;
import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.entity.Store;
import com.example.demo.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
public class CouponTests {

    @Autowired
    private CustomerCouponService customerCouponService;

    @Autowired
    private EntityManager em;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CustomerCouponRepository customerCouponRepository;

    @Test
    void checkNPlusOneProblem() {
        // given
        Long customerId = 1L;
        Long storeId = 1L;

        // Hibernate SQL 로그 보기 쉽게 설정
        em.flush();
        em.clear();

        // when
        customerCouponService.getMyUsableCouponsInStore(customerId, storeId);

        // then
        // 콘솔 로그 확인: N+1 발생 여부는 Hibernate가 뿜는 SQL 로그로 확인
    }

    @Test
    void checkNPlusOneProblem1() {
            // given
            Customer customer = customerRepository.save(Customer.builder().build());

            for (int i = 0; i < 5; i++) {
                Store store = storeRepository.save(Store.builder()
                        .storeName("store" + i)
                        .ownerEmail("store" + i + "@test.com")
                        .password("pass")
                        .provider("local")
                        .build());
                em.flush();
                em.clear();
                Coupon coupon = couponRepository.save(Coupon.builder()
                        .couponName("coupon" + i)
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(1000)
                        .expiryType(ExpiryType.ABSOLUTE)
                        .expiryDate(LocalDateTime.now().plusDays(10))
                        .totalQuantity(100)
                        .store(store)
                        .build());

                CustomerCoupon cc = CustomerCoupon.builder()
                        .couponUuid(String.valueOf(UUID.randomUUID()))
                        .coupon(coupon)
                        .customer(customer)
                        .couponStatus(CouponStatus.UNUSED)
                        .expiresAt(LocalDateTime.now().plusDays(5))
                        .issuedAt(LocalDateTime.now())
                        .build();

                customerCouponRepository.save(cc);
            }



            // when
            List<CustomerCouponDto> result = customerCouponService.getMyUsableCouponsInStore(customer.getId(), 1L);
            System.out.println("조회된 쿠폰 수: " + result.size());

            // then: Hibernate 로그에서 Coupon / Store 쿼리가 반복되면 N+1 발생
        }

}
