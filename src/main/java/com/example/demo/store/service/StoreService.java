package com.example.demo.store.service;

import com.example.demo.setting.exception.BusinessException;
import com.example.demo.setting.exception.ErrorCode;
import com.example.demo.store.dto.MenuSalesStatisticsDto;
import com.example.demo.store.dto.StoreRegistrationDTO;
import com.example.demo.store.dto.StoreSalesResponseDto;
import com.example.demo.store.entity.QR_Code;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.repository.CustomerCouponRepository;
import com.example.demo.customer.repository.CustomerStatisticsRepository;
import com.example.demo.store.repository.QRCodeRepository;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.setting.util.TokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final QRCodeRepository qrCodeRepository;
    private final StoreRepository storeRepository;
    private final CustomerCouponRepository customerCouponRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerStatisticsRepository customerStatisticsRepository;
    private final TokenRedisService tokenRedisService;
    private final com.example.demo.setting.service.EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional // 회원가입
    public Store registerStore(StoreRegistrationDTO storeRegistrationDTO) {
        // 이메일 중복 검사
        if (storeRepository.findByOwnerEmail(storeRegistrationDTO.getOwnerEmail()).isPresent()) {
            log.warn("[매장 회원가입 실패] 이미 존재하는 이메일: {}", storeRegistrationDTO.getOwnerEmail());
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 이메일 인증 토큰 생성
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24); // 24시간 후 만료

        // DTO에서 Entity로 변환 (필요한 값만 설정)
        Store store = Store.builder()
                .storeName(storeRegistrationDTO.getStoreName())
                .ownerEmail(storeRegistrationDTO.getOwnerEmail())
                .password(passwordEncoder.encode(storeRegistrationDTO.getPassword())) // 비밀번호 암호화
                .provider("local")
                .emailVerified(false) // 초기값은 미인증
                .emailVerificationToken(verificationToken)
                .emailVerificationExpiry(expiryTime)
                .build();
        store = storeRepository.save(store);
        createQRCode(store); // QR코드 생성 로직 호출

        // 이메일 인증 링크 발송
        emailService.sendEmailVerification(
                store.getOwnerEmail(),
                store.getStoreName(),
                verificationToken
        );

        log.info("[매장 회원가입 성공] 이메일: {}, 매장명: {}", store.getOwnerEmail(), store.getStoreName());
        return store;
    }

    @Transactional // 회원 탈퇴
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        log.info("[매장 탈퇴 시작] 매장 ID: {}, 매장명: {}", storeId, store.getStoreName());

        // 1. 해당 상점의 쿠폰을 발급받은 모든 고객 쿠폰 삭제
        List<Coupon> storeCoupons = store.getCoupons();
        for (Coupon coupon : storeCoupons) {
            // 해당 쿠폰을 발급받은 모든 고객 쿠폰 삭제
            customerCouponRepository.deleteByCoupon(coupon);
            log.info("[고객 쿠폰 삭제] 쿠폰 ID: {}, 쿠폰명: {}", coupon.getId(), coupon.getCouponName());
        }

        // 2. 매장 메뉴의 이미지 파일 삭제 (파일 시스템에서)
        List<StoreMenu> menus = store.getStoreMenu();
        for (StoreMenu menu : menus) {
            if (menu.getImageUrl() != null && !menu.getImageUrl().isEmpty()) {
                try {
                    // 실제 파일 삭제 로직 (필요에 따라 구현)
                    deleteMenuImageFile(menu.getImageUrl());
                    log.info("[메뉴 이미지 삭제] 메뉴: {}, 이미지: {}", menu.getMenuName(), menu.getImageUrl());
                } catch (Exception e) {
                    log.warn("[메뉴 이미지 삭��� 실패] 메뉴: {}, 이미지: {}, 에러: {}",
                        menu.getMenuName(), menu.getImageUrl(), e.getMessage());
                }
            }
        }

        // 3. Redis에서 해당 상점의 토큰 정보 삭제
        try {
            tokenRedisService.deleteRefreshToken(store.getOwnerEmail());
            log.info("[Redis 토큰 삭제] 이메일: {}", store.getOwnerEmail());
        } catch (Exception e) {
            log.warn("[Redis 토큰 삭제 실패] 이메일: {}, 에러: {}", store.getOwnerEmail(), e.getMessage());
        }

        // 4. Store 엔티티 삭제 (cascade로 인해 연관된 엔티티들이 자동 삭제됨)
        // - QR_Code (QR코드)
        // - StoreMenu (메뉴 및 리뷰)
        // - OrderStatus (주문 상태)
        // - CustomerStatistics (고객 통계)
        // - Coupon (쿠폰)
        storeRepository.delete(store);

        log.info("[매장 탈퇴 완료] 매장 ID: {}, 매장명: {}, 이메일: {}",
            storeId, store.getStoreName(), store.getOwnerEmail());
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

        // 이메일 인증 확인
        if (!store.isEmailVerified()) {
            log.warn("[매장 로그인 실패] 이메일 미인증. 이메일: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        log.info("[매장 로그인 성공] 이메일: {}, 매장명: {}", store.getOwnerEmail(), store.getStoreName());
        return store;
    }

    /**
     * 이메일 인증 처리
     */
    @Transactional
    public void verifyEmail(String token) {
        Store store = storeRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> {
                    log.warn("[이메일 인증 실패] 유효하지 않은 토큰: {}", token);
                    return new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN);
                });

        if (store.isEmailVerificationExpired()) {
            log.warn("[이메일 인증 실패] 만료된 토큰. 이메일: {}", store.getOwnerEmail());
            throw new BusinessException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }

        store.verifyEmail();
        storeRepository.save(store);
        log.info("[이메일 인증 성공] 이메일: {}", store.getOwnerEmail());
    }

    /**
     * 이메일 인증 토큰 재발급
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (store.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // 새로운 인증 토큰 생성
        String newToken = UUID.randomUUID().toString();
        LocalDateTime newExpiryTime = LocalDateTime.now().plusHours(24);

        store.setEmailVerificationToken(newToken, newExpiryTime);
        storeRepository.save(store);

        // 새 인증 이메일 발송
        emailService.sendEmailVerification(
                store.getOwnerEmail(),
                store.getStoreName(),
                newToken
        );

        log.info("[이메일 인증 토큰 재발급] 이메일: {}", email);
    }

    /**
     * 매장 정보 업데이트
     */
    @Transactional
    public void updateStoreInfo(String email, com.example.demo.store.dto.StoreUpdateDTO updateDTO) {
        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 매장명 업데이트
        if (updateDTO.getStoreName() != null && !updateDTO.getStoreName().trim().isEmpty()) {
            store.updateStoreName(updateDTO.getStoreName().trim());
            log.info("[매장명 변경] 이메일: {}, 기존: {} → 신규: {}", email, store.getStoreName(), updateDTO.getStoreName());
        }

        // 비밀번호 업데이트
        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().trim().isEmpty()) {
            // 현재 비밀번호 확인
            if (updateDTO.getCurrentPassword() == null || !passwordEncoder.matches(updateDTO.getCurrentPassword(), store.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_EXCEPTION);
            }

            // 새 비밀번호와 확인 비밀번호 일치 확인
            if (!updateDTO.getNewPassword().equals(updateDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            }

            // 비밀번호 암호화 후 업데이트
            String encodedNewPassword = passwordEncoder.encode(updateDTO.getNewPassword());
            store.updatePassword(encodedNewPassword);
            log.info("[비밀번호 변경] 이메일: {}", email);
        }

        storeRepository.save(store);
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 비밀번호 재설정 토큰 생성
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1); // 1시간 후 만료

        store.setPasswordResetToken(resetToken, expiryTime);
        storeRepository.save(store);

        // 비밀번호 재설정 이메일 발송
        emailService.sendPasswordResetEmail(
                store.getOwnerEmail(),
                store.getStoreName(),
                resetToken
        );

        log.info("[비밀번호 재설정 이메일 발송] 이메일: {}", email);
    }

    /**
     * 비밀번호 재설정 처리
     */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        Store store = storeRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> {
                    log.warn("[비밀번호 재설정 실패] 유효하지 않은 토큰: {}", token);
                    return new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN);
                });

        if (store.isPasswordResetTokenExpired()) {
            log.warn("[비밀번호 재설정 실패] 만료된 토큰. 이메일: {}", store.getOwnerEmail());
            // 만료된 토큰 정리
            store.clearPasswordResetToken();
            storeRepository.save(store);
            throw new BusinessException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }

        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 업데이트 및 토큰 삭제
        String encodedPassword = passwordEncoder.encode(newPassword);
        store.updatePassword(encodedPassword);
        store.clearPasswordResetToken();

        storeRepository.save(store);
        log.info("[비밀번호 재설정 성공] 이메일: {}", store.getOwnerEmail());
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

    /**
     * 메뉴 이미지 파일 삭제 (파일 시스템에서)
     */
    private void deleteMenuImageFile(String imageUrl) {
        // 이미지 URL에서 실제 파일 경로 추출 후 삭제
        // 예: /uploads/menu/12345.jpg -> 실제 파일 시스템의 파일 삭제
        try {
            if (imageUrl.startsWith("/uploads/")) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get("uploads/menu", fileName);
                Files.deleteIfExists(filePath);
                log.debug("[파일 삭제 성공] 경로: {}", filePath);
            }
        } catch (Exception e) {
            log.error("[파일 삭제 실패] 이미지 URL: {}, 에러: {}", imageUrl, e.getMessage());
        }
    }
    /**
     storeId로 Store 엔티티를 조회
     */
    @Transactional(readOnly = true)
    public Store findById(Long storeId) {
        return storeRepository.findById(storeId)
        .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }
}
