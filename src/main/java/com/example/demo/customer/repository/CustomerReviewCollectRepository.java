package com.example.demo.customer.repository;

import com.example.demo.customer.entity.CustomerReviewCollect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerReviewCollectRepository extends JpaRepository<CustomerReviewCollect, Long> {
    List<CustomerReviewCollect> findByStoreMenu_MenuId(Long menuId);
}
