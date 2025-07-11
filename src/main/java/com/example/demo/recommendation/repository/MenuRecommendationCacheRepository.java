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
    // 유효한 캐시 조회 (가장 최신 것 하나만)
    @Query(value = "SELECT * FROM menu_recommendation_cache m " +
           "WHERE m.store_id = :storeId " +
           "AND m.expired_at > :now " +
           "ORDER BY m.created_at DESC " +
           "LIMIT 1", nativeQuery = true)
    Optional<MenuRecommendationCache> findValidCacheByStoreId(
        @Param("storeId") Long storeId,
        @Param("now") LocalDateTime now
    );

    // 매장별 최신 추천 조회
    Optional<MenuRecommendationCache> findTopByStoreOrderByCreatedAtDesc(Store store);

    // 매장별 캐시 삭제
    @Modifying
    @Query("DELETE FROM MenuRecommendationCache m WHERE m.store.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    // 만료된 캐시 삭제
    @Modifying
    @Query("DELETE FROM MenuRecommendationCache m WHERE m.expiredAt < :now")
    void deleteExpiredCache(@Param("now") LocalDateTime now);

    // 특정 조건의 캐시 조회 (중복 방지용)
    Optional<MenuRecommendationCache> findByStoreAndWeatherConditionAndSeason(
        Store store, String weatherCondition, String season
    );

    // 캐시 내용 업데이트 (UPSERT용)
    @Modifying
    @Query("UPDATE MenuRecommendationCache m SET m.gptRecommendation = :content, m.expiredAt = :expiredAt WHERE m.id = :id")
    void updateCacheContent(@Param("id") Long id, @Param("content") String content, @Param("expiredAt") LocalDateTime expiredAt);

    // 최근 캐시 조회 (새로고침 방지용)
    @Query(value = "SELECT * FROM menu_recommendation_cache m " +
           "WHERE m.store_id = :storeId " +
           "AND m.created_at > :since " +
           "ORDER BY m.created_at DESC " +
           "LIMIT 1", nativeQuery = true)
    Optional<MenuRecommendationCache> findRecentCacheByStoreId(
        @Param("storeId") Long storeId,
        @Param("since") LocalDateTime since
    );
}
