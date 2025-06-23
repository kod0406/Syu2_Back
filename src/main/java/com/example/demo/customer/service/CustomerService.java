package com.example.demo.customer.service;

import com.example.demo.customer.dto.CustomerStatisticsDto;
import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.repository.CustomerStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerStatisticsRepository customerStatisticsRepository;

    public List<CustomerStatisticsDto> customerStatistics(Customer customer, String storeName) {
        if (customer == null || storeName == null || storeName.isBlank()) {
            log.warn("고객 또는 매장명이 유효하지 않습니다.");
            return List.of();
        }

        return customerStatisticsRepository.findCustomerStatisticsByStoreName(customer.getCustomerId(), storeName);
    }
}
