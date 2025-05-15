package com.example.demo.repository;

import com.example.demo.entity.store.Store;
import com.example.demo.entity.store.StoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreMenuRepository extends JpaRepository<StoreMenu, Long> {
    // 메뉴 이름으로 검색
    StoreMenu findByMenuName(String menuName);

    // 매장 ID로 메뉴 검색
    StoreMenu findByStoreId(Long storeId);

    // 매장 ID와 메뉴 이름으로 메뉴 검색
    StoreMenu findByStoreIdAndMenuName(Long storeId, String menuName);

    List<StoreMenu> findByStore(Store store);

    List<StoreMenu> findByStoreAndCategory(Store store, String category);

    @Query("SELECT DISTINCT m.category FROM StoreMenu m WHERE m.store = :store AND m.category IS NOT NULL")
    List<String> findCategoriesByStore(@Param("store")Store store);
}
