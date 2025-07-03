package com.example.demo.recommendation.repository;

import com.example.demo.store.entity.MenuRecommendationCache;
import com.example.demo.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRecommendationCacheRepository extends JpaRepository<MenuRecommendationCache, Long> {
    // 유효한 캐시 조회
    @Query("SELECT m FROM MenuRecommendationCache m " +
           "WHERE m.store.storeId = :storeId " +
           "AND m.expiredAt > :now " +
           "ORDER BY m.createdAt DESC")
    Optional<MenuRecommendationCache> findValidCacheByStoreId(
        @Param("storeId") Long storeId,
        @Param("now") LocalDateTime now
    );

    // 매장별 최신 추천 조회
    Optional<MenuRecommendationCache> findTopByStoreOrderByCreatedAtDesc(Store store);

    // 만료된 캐시 삭제
    @Modifying
    @Query("DELETE FROM MenuRecommendationCache m WHERE m.expiredAt < :now")
    void deleteExpiredCache(@Param("now") LocalDateTime now);

    // 매장별 추천 히스토리 조회
    List<MenuRecommendationCache> findByStoreAndCreatedAtAfterOrderByCreatedAtDesc(
        Store store, LocalDateTime since
    );
}
