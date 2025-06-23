package com.example.demo.customer.repository;

import com.example.demo.customer.dto.CustomerStatisticsDto;
import com.example.demo.store.dto.MenuSalesStatisticsDto;
import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CustomerStatisticsRepository extends JpaRepository<CustomerStatistics, Long> {
    List<CustomerStatistics> findByCustomerAndReviewedFalse(Customer customer);
//    List<CustomerStatistics> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
//    List<CustomerStatistics> findByCreatedAtAfter(LocalDateTime dateTime);

    @Query("""
    SELECT new com.example.demo.store.dto.MenuSalesStatisticsDto(
        m.menuName,
        m.imageUrl,
        SUM(s.orderAmount),
        SUM(s.orderAmount * s.orderPrice)
    )
    FROM CustomerStatistics s, StoreMenu m
    WHERE s.orderDetails = m.menuName
      AND s.store.storeId = m.store.storeId
      AND s.store.storeId = :storeId
      AND s.date BETWEEN :start AND :end
    GROUP BY m.menuName, m.imageUrl
""")
    List<MenuSalesStatisticsDto> getMenuStatsWithoutRelation(
            @Param("storeId") Long storeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
    SELECT new com.example.demo.customer.dto.CustomerStatisticsDto(
        m.menuName,
        m.imageUrl,
        SUM(s.orderAmount),
        SUM(s.orderAmount * s.orderPrice)
    )
    FROM CustomerStatistics s, StoreMenu m
    WHERE s.orderDetails = m.menuName
      AND s.store.storeId = m.store.storeId
      AND s.customer.customerId = :customerId
      AND s.store.storeName = :storeName
    GROUP BY m.menuName, m.imageUrl
""")
    List<CustomerStatisticsDto> findCustomerStatisticsByStoreName(
            @Param("customerId") Long customerId,
            @Param("storeName") String storeName
    );


}
