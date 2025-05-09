package com.example.demo.Service;

import com.example.demo.dto.StoreRegistrationDTO;
import com.example.demo.entity.store.Store;
import com.example.demo.repository.StoreRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public StoreService(StoreRepository storeRepository, PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // 회원가입
    public Store registerStore(StoreRegistrationDTO storeRegistrationDTO) {
        // 이메일 중복 검사
        if (storeRepository.findByOwnerEmail(storeRegistrationDTO.getOwnerEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // DTO에서 Entity로 변환 (필요한 값만 설정)
        Store store = Store.builder()
                .storeName(storeRegistrationDTO.getStoreName())
                .ownerEmail(storeRegistrationDTO.getOwnerEmail())
                .password(passwordEncoder.encode(storeRegistrationDTO.getPassword())) // 비밀번호 암호화
                .provider("local")
                .build();

        return storeRepository.save(store);
    }

    @Transactional // 회원 탈퇴
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        // 추후 연관된 데이터 삭제 로직 추가 가능
        // (예: 매장 메뉴, 리뷰 등 삭제)

        storeRepository.delete(store);
    }
}