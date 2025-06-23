package com.example.demo.order.repository;

import com.example.demo.order.entity.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    List<OrderGroup> findAllByStoreIdAndActiveFalse(Long storeId);
}
