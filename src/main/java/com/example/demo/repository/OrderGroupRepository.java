package com.example.demo.repository;

import com.example.demo.entity.common.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    List<OrderGroup> findAllByStoreIdAndActiveFalse(Long storeId);
}
