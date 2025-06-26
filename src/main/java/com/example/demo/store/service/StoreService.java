package com.example.demo.store.service;

import com.example.demo.setting.exception.BusinessException;
import com.example.demo.setting.exception.ErrorCode;
import com.example.demo.store.dto.MenuSalesStatisticsDto;
import com.example.demo.store.dto.StoreRegistrationDTO;
import com.example.demo.store.dto.StoreSalesResponseDto;
import com.example.demo.store.entity.QR_Code;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.setting.jwt.JwtTokenProvider;
import com.example.demo.customer.repository.CustomerStatisticsRepository;
import com.example.demo.store.repository.QRCodeRepository;
import com.example.demo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final QRCodeRepository qrCodeRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerStatisticsRepository customerStatisticsRepository;

    @Value("${frontend.url}")
    private String frontendUrl;



    @Transactional // 회원가입
    public Store registerStore(StoreRegistrationDTO storeRegistrationDTO) {
        // 이메일 중복 검사
        if (storeRepository.findByOwnerEmail(storeRegistrationDTO.getOwnerEmail()).isPresent()) {
            log.warn("[매장 회원가입 실패] 이미 존재하는 이메일: {}", storeRegistrationDTO.getOwnerEmail());
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        // DTO에서 Entity로 변환 (필요한 값만 설정)
        Store store = Store.builder()
                .storeName(storeRegistrationDTO.getStoreName())
                .ownerEmail(storeRegistrationDTO.getOwnerEmail())
                .password(passwordEncoder.encode(storeRegistrationDTO.getPassword())) // 비밀번호 암호화
                .provider("local")
                .build();
        store = storeRepository.save(store);
        createQRCode(store); // QR코드 생성 로직 호출
        log.info("[매장 회원가입 성공] 이메일: {}, 매장명: {}", store.getOwnerEmail(), store.getStoreName());
        return store;
    }

    @Transactional // 회원 탈퇴
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 추후 연관된 데이터 삭제 로직 추가 가능
        // (예: 매장 메뉴, 리뷰 등 삭제)
        //TODO 나중에 손님이 발급 받은 쿠폰도 삭제하거나 '사용불가 '처리가 필요

        storeRepository.delete(store);
    }

    public void createQRCode(Store store){
        // QR 코드에 포함될 URL을 지정
        String menuUrl = frontendUrl + "/menu/" + store.getStoreId();


        // QR 코드 엔티티 생성
        QR_Code qrCode = QR_Code.builder()
                .QR_Code(menuUrl) // QR_Code 필드에 menuUrl 저장
                .store(store)
                .build();

        // QR 코드 저장
        qrCodeRepository.save(qrCode);

    }

    public Store authenticateStore(String email, String password) {
        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> {
                    log.warn("[매장 로그인 실패] 존재하지 않는 이메일: {}", email);
                    return new BusinessException(ErrorCode.STORE_NOT_FOUND);
                });
        // 비밀번호 검증 로직 (예: BCrypt 사용)
        if (!passwordEncoder.matches(password, store.getPassword())) {
            log.warn("[매장 로그인 실패] 비밀번호 불일치. 이메일: {}", email);
            throw new BusinessException(ErrorCode.PASSWORD_EXCEPTION);
        }
        log.info("[매장 로그인 성공] 이메일: {}, 매장명: {}", store.getOwnerEmail(), store.getStoreName());
        return store;
    }

    @Transactional(readOnly = true)
    public StoreSalesResponseDto getStoreSales(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        List<StoreMenu> menus = store.getStoreMenu();

        long dailyTotalRevenue = 0L;
        long totalRevenue = 0L;
        int dailyTotalSales = 0;
        int totalSales = 0;

        for (StoreMenu menu : menus) {
            dailyTotalRevenue += menu.getDailyRevenue();
            totalRevenue += menu.getRevenue();
            dailyTotalSales += menu.getDailySales();
            totalSales += menu.getTotalSales();
        }

        return new StoreSalesResponseDto(
                storeId,
                dailyTotalRevenue,
                totalRevenue,
                dailyTotalSales,
                totalSales
        );
    }

    public List<MenuSalesStatisticsDto> storeStatistics(Store store, LocalDate start, LocalDate end) {
        Long storeId = store.getStoreId();
        return customerStatisticsRepository.getMenuStatsWithoutRelation(storeId, start, end);
    }
}