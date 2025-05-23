package com.example.demo.Service;

import com.example.demo.KakaoPay.KakaoPayProvider;
import com.example.demo.KakaoPay.KakaoPayRequest;
import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.common.OrderGroup;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.customer.CustomerPoint;
import com.example.demo.entity.store.Store;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Transactional
    public KakaoPayRequest.OrderRequest order(List<OrderDTO> orders, Customer customer, Long storeId) {
        int totalAmount = 0;
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장이 존재하지 않습니다."));

        OrderGroup orderGroup = OrderGroup.builder()
                .customer(customer)
                .storeId(storeId)
                .createdAt(LocalDateTime.now())
                .build();
        orderGroupRepository.save(orderGroup);

        //추후 느려질 때를 대비해야함.
        for (OrderDTO dto : orders) {
            if ("UserPointUsedOrNotUsed".equals(dto.getMenuName()) && customer == null) {
                log.warn("⚠️ 고객 없음 - 포인트 항목 저장 스킵: {}", dto);
                continue;
            }

            CustomerStatistics customerStatistics = CustomerStatistics.builder()
                    .store(store)
                    .orderDetails(dto.getMenuName())
                    .orderAmount(dto.getMenuAmount())
                    .date(LocalDate.now())
                    .orderPrice(dto.getMenuPrice())
                    .customer(customer)
                    .orderGroup(orderGroup)
                    .build();
            //저장 로직
            customerStatisticsRepository.save(customerStatistics);
            totalAmount += dto.getMenuPrice() * dto.getMenuAmount();
        }
        
        String representativeMenu = orders.isEmpty() ? "메뉴 없음" : orders.get(0).getMenuName();
        if (orders.size() > 1) {
            representativeMenu += " 외 " + (orders.size() - 1) + "개";
        }
        KakaoPayRequest.OrderRequest orderRequest = KakaoPayRequest.OrderRequest.builder()
                .itemName(representativeMenu)
                .totalPrice(String.valueOf(totalAmount))
                .quantity(String.valueOf(orders.size())) // 또는 주문 이름 리스트만 추출할 수도 있음
                .OrderGroup(orderGroup)
                .build();

        return orderRequest;
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