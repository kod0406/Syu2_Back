package com.example.demo.recommendation.repository;

import com.example.demo.store.entity.MenuRecommendationHistory;
import com.example.demo.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MenuRecommendationHistoryRepository extends JpaRepository<MenuRecommendationHistory, Long> {

    // 매장별 추천 히스토리 조회 (날짜 범위)
    List<MenuRecommendationHistory> findByStoreAndCreatedAtAfterOrderByCreatedAtDesc(
        Store store, LocalDateTime since
    );

    // 매장별 전체 히스토리 조회
    List<MenuRecommendationHistory> findByStoreOrderByCreatedAtDesc(Store store);

    // 매장별 히스토리 삭제
    @Modifying
    @Query("DELETE FROM MenuRecommendationHistory m WHERE m.store.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
