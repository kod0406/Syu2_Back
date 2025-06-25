package com.example.demo.customer.service;

import com.example.demo.KakaoPay.KakaoPayRequest;
import com.example.demo.benefit.dto.CustomerCouponDto;
import com.example.demo.benefit.repository.CustomerPointRepository;
import com.example.demo.benefit.service.CustomerCouponService;
import com.example.demo.customer.repository.CustomerStatisticsRepository;
import com.example.demo.order.dto.OrderDTO;
import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.order.entity.OrderGroup;
import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.entity.CustomerPoint;
import com.example.demo.store.entity.Store;
import com.example.demo.order.repository.OrderGroupRepository;
import com.example.demo.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderService {
    private final CustomerPointRepository customerPointRepository;
    private final CustomerStatisticsRepository customerStatisticsRepository;
    private final StoreRepository storeRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final CustomerCouponService customerCouponService;

    @Transactional
    public KakaoPayRequest.OrderRequest order(List<OrderDTO> orders, Customer customer, Long storeId) {
        long totalAmount = 0;

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장이 존재하지 않습니다."));

        OrderGroup orderGroup = OrderGroup.builder()
                .customer(customer)
                .storeId(storeId)
                .createdAt(LocalDateTime.now())
                .build();
        orderGroupRepository.save(orderGroup);

        for (OrderDTO dto : orders) {
            if ("UserPointUsedOrNotUsed".equals(dto.getMenuName()) && customer == null) {
                log.warn("⚠️ 고객 없음 - 포인트 항목 저장 스킵: {}", dto);
                continue;
            }

            long adjustedPrice = dto.getMenuPrice();
            String orderDetails = dto.getMenuName(); // 프론트에서 받은 데이터를 그대로 저장
            int orderAmount = Math.toIntExact(dto.getMenuAmount());

            // 쿠폰 또는 포인트 사용 시, 가격을 음수로 조정
            if (orderDetails.startsWith("CouponUsed:") || orderDetails.equals("UserPointUsedOrNotUsed")) {
                adjustedPrice = -adjustedPrice;
            }

            CustomerStatistics customerStatistics = CustomerStatistics.builder()
                    .store(store)
                    .orderDetails(orderDetails) // "CouponUsed:[UUID]" 또는 "UserPointUsedOrNotUsed"가 저장됨
                    .orderAmount(orderAmount)
                    .date(LocalDate.now())
                    .orderPrice(adjustedPrice)
                    .customer(customer)
                    .orderGroup(orderGroup)
                    .build();

            customerStatisticsRepository.save(customerStatistics);
            totalAmount += adjustedPrice * dto.getMenuAmount();
        }

        String representativeMenu = orders.isEmpty() ? "메뉴 없음" : orders.get(0).getMenuName();
        if (orders.size() > 1) {
            representativeMenu += " 외 " + (orders.size() - 1) + "개";
        }

        return KakaoPayRequest.OrderRequest.builder()
                .itemName(representativeMenu)
                .totalPrice(String.valueOf(totalAmount))
                .quantity(String.valueOf(orders.size()))
                .OrderGroup(orderGroup)
                .build();
    }

    @Transactional
    public void processPaymentFailure(Long orderGroupId) {
        orderGroupRepository.findById(orderGroupId).ifPresent(orderGroup -> {
            customerStatisticsRepository.deleteAll(orderGroup.getCustomerStatisticsList());
            orderGroupRepository.delete(orderGroup); // 주문 자체 삭제
            log.info("❌ 결제 실패: 주문 {} 관련 통계 및 그룹 삭제됨", orderGroupId);
        });
    }

    public long getPoint(Customer customer) {
        return customerPointRepository.findByCustomer(customer)
                .map(CustomerPoint::getPointAmount)
                .orElse(0L); // 포인트 정보 없으면 0
    }
}