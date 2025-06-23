package com.example.demo.store.repository;

import com.example.demo.store.entity.StoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<StoreMenu, Long> {
    List<StoreMenu> findAllByStore_StoreId(Long storeId);
}
