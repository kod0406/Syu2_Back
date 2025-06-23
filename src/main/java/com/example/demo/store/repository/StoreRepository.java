package com.example.demo.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.store.entity.Store;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByOwnerEmail(String email);
}
