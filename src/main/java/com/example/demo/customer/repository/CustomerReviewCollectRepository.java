package com.example.demo.customer.repository;

import com.example.demo.customer.entity.CustomerReviewCollect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerReviewCollectRepository extends JpaRepository<CustomerReviewCollect, Long> {
    List<CustomerReviewCollect> findByStoreMenu_MenuId(Long menuId);

    // 매장의 특정 메뉴 리뷰 조회 (StoreMenuReviewController에서 사용)
    @Query("SELECT r FROM CustomerReviewCollect r " +
           "WHERE r.store.storeId = :storeId " +
           "AND r.storeMenu.menuId = :menuId " +
           "ORDER BY r.reviewDate DESC")
    List<CustomerReviewCollect> findByStoreIdAndMenuId(@Param("storeId") Long storeId,
                                                       @Param("menuId") Long menuId);

    // 매장별 전체 리뷰 조회 (메인 리뷰 조회용 - StoreMenuReviewController와 ReviewAnalyzer에서 사용)
    List<CustomerReviewCollect> findByStore_StoreIdOrderByReviewDateDesc(Long storeId);

    // - findAllByStoreId: findByStore_StoreIdOrderByReviewDateDesc로 대체됨
}
