package com.example.demo.order.repository;

import com.example.demo.customer.entity.Customer;
import com.example.demo.order.entity.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    List<OrderGroup> findAllByStoreIdAndActiveFalse(Long storeId);

    List<OrderGroup> findAllByStoreIdAndActiveFalseAndApprovedTrue(Long storeId);

    @Modifying
    @Query("DELETE FROM OrderGroup o WHERE o.customer = :customer AND o.approved = :approved")
    void deleteAllByCustomerAndApproved(@Param("customer") Customer customer, @Param("approved") boolean approved);
}
