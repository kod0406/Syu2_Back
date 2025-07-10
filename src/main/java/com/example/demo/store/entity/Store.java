package com.example.demo.store.entity;

import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.order.entity.OrderStatus;
import com.example.demo.benefit.entity.Coupon;
import com.example.demo.user.entity.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Store implements AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long storeId;

    private String storeName;

    private String ownerEmail;

    private String password;

    private String provider;

    // 이메일 인증 관련 필드
    @Builder.Default
    private boolean emailVerified = false;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationExpiry;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("store-qr")
    private List<QR_Code> qr_Code = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("store-menu")
    private List<StoreMenu> storeMenu = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("store-order-status")
    private List<OrderStatus> orderStatus = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("store-customer-statistics")
    private List<CustomerStatistics> customerStatistics = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("store-coupons")
    private List<Coupon> coupons = new ArrayList<>();

    @Override
    public Long getId() {
        return storeId;
    }

    @Override
    public String getEmail() {
        return ownerEmail;
    }

    @Override
    public String getRole() {
        return "ROLE_STORE";
    }
    // 매장 이름으로 검색

    // 이메일 인증 관련 메서드
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiry = null;
    }

    public void setEmailVerificationToken(String token, LocalDateTime expiry) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiry = expiry;
    }

    public boolean isEmailVerificationExpired() {
        return emailVerificationExpiry != null && LocalDateTime.now().isAfter(emailVerificationExpiry);
    }

    // 매장 정보 업데이트 메서드
    public void updateStoreName(String newStoreName) {
        this.storeName = newStoreName;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 비밀번호 재설정 관련 필드 및 메서드
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;

    public void setPasswordResetToken(String token, LocalDateTime expiry) {
        this.passwordResetToken = token;
        this.passwordResetExpiry = expiry;
    }

    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpiry = null;
    }

    public boolean isPasswordResetTokenExpired() {
        // 토큰이나 만료시간이 없으면 만료된 것으로 간주
        if (passwordResetToken == null || passwordResetExpiry == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(passwordResetExpiry);
    }
}
