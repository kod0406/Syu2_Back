package com.example.demo.repository;

import com.example.demo.entity.common.CustomerReviewCollect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerReviewCollectRepository extends JpaRepository<CustomerReviewCollect, Long> {
    List<CustomerReviewCollect> findByStoreMenu_MenuId(Long menuId);
}
