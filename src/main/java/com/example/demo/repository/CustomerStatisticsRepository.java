package com.example.demo.repository;

import com.example.demo.entity.common.CustomerStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerStatisticsRepository extends JpaRepository<CustomerStatistics, Long> {
}
