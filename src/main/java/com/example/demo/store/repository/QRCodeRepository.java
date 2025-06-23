package com.example.demo.store.repository;

import com.example.demo.store.entity.QR_Code;
import com.example.demo.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QR_Code,Long> {
    Optional<QR_Code> findByStore(Store store);
    Optional<QR_Code> findByStoreStoreId(Long storeId);
}
