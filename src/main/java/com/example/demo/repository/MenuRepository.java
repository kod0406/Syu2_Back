package com.example.demo.repository;

import com.example.demo.entity.store.StoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<StoreMenu, Long> {
    List<StoreMenu> findAllByStore_StoreId(Long storeId);
}
