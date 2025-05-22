package com.example.demo.repository;

import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.customer.CustomerPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerPointRepository extends JpaRepository<CustomerPoint, Long> {
    Optional<CustomerPoint> findByCustomer(Customer customer);
}
