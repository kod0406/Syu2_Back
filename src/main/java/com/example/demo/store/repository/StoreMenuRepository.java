package com.example.demo.store.repository;

import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.store.entity.StoreMenuReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StoreMenuRepository extends JpaRepository<StoreMenu, Long> {
    // 메뉴 이름으로 검색
    StoreMenu findByMenuName(String menuName);

    // 매장 ID로 메뉴 검색
    StoreMenu findByStoreId(Long storeId);

    // 매장 ID와 메뉴 이름으로 메뉴 검색
    StoreMenu findByStoreIdAndMenuName(Long storeId, String menuName);

    List<StoreMenu> findByStore(Store store);

    // 매장별 리뷰 조회 (ReviewAnalyzer에서 사용)
    @Query("SELECT r FROM StoreMenuReview r " +
           "WHERE r.storeMenu.store.storeId = :storeId " +
           "AND r.reviewDate >= :since " +
           "ORDER BY r.reviewDate DESC")
    List<StoreMenuReview> findReviewsByStoreIdAndDate(@Param("storeId") Long storeId,
                                                     @Param("since") LocalDateTime since);

    List<StoreMenu> findByStoreAndCategory(Store store, String category);

    // 각 상점의 메뉴 개수를 조회
    long countByStoreStoreId(Long storeId);

    @Query("SELECT DISTINCT m.category FROM StoreMenu m WHERE m.store = :store AND m.category IS NOT NULL")
    List<String> findCategoriesByStore(@Param("store")Store store);
}
