package com.example.demo.repository;

import com.example.demo.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.store.Store;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByOwnerEmail(String email);
}
