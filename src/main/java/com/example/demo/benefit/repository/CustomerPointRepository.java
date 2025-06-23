package com.example.demo.benefit.repository;

import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.entity.CustomerPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerPointRepository extends JpaRepository<CustomerPoint, Long> {
    Optional<CustomerPoint> findByCustomer(Customer customer);
}
