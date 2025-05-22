package com.example.demo.Service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.customer.CustomerPoint;
import com.example.demo.repository.CustomerPointRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.CustomerStatisticsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {
    private final CustomerPointRepository customerPointRepository;
    private final CustomerRepository customerRepository;
    private final CustomerStatisticsRepository customerStatisticsRepository;

    @Transactional
    public void order(List<OrderDTO> orders, Customer customer) {
        int totalAmount = 0;

        for (OrderDTO dto : orders) {
            CustomerStatistics customerStatistics = CustomerStatistics.builder()
                    .orderDetails(dto.getMenuName())
                    .orderAmount(dto.getMenuAmount())
                    .orderPrice(dto.getMenuPrice())
                    .customer(customer) // 로그인 회원만 연결, 비회원은 null
                    .build();

            customerStatisticsRepository.save(customerStatistics);
            totalAmount += dto.getMenuPrice() * dto.getMenuAmount();
        }

        // 로그인 회원일 경우에만 포인트 적립
        if (customer != null) {
            int point = (int) (totalAmount * 0.01);

            CustomerPoint customerPoint = customerPointRepository.findByCustomer(customer)
                    .orElseGet(() -> CustomerPoint.builder()
                            .customer(customer)
                            .pointAmount(0L)
                            .build());

            customerPoint.addPoint(point);
            customerPointRepository.save(customerPoint);
        }
    }
}