package com.example.demo.store.repository;
//손님 위치 기반 근처 매장 검색으로 확장 가능
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreLocationRepository extends JpaRepository<StoreLocation, Long> {
    Optional<StoreLocation> findByStore(Store store);

    // 위치 정보가 등록된 활성 매장들 조회
    @Query("SELECT sl.store FROM StoreLocation sl " +
           "WHERE sl.latitude IS NOT NULL " +
           "AND sl.longitude IS NOT NULL")
    List<Store> findStoresWithLocation();

    // 특정 지역의 매장들 조회
    @Query("SELECT sl FROM StoreLocation sl " +
           "WHERE sl.city LIKE %:city% " +
           "AND sl.latitude IS NOT NULL")
    List<StoreLocation> findByCity(@Param("city") String city);
}

