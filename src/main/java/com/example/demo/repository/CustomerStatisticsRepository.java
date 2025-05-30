package com.example.demo.repository;

import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerStatisticsRepository extends JpaRepository<CustomerStatistics, Long> {
    List<CustomerStatistics> findByCustomerAndReviewedFalse(Customer customer);
}
