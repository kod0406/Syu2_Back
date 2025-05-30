package com.example.demo.Controller;

import com.example.demo.Service.coupon.CouponService;
import com.example.demo.dto.coupon.CouponCreateRequestDto;
import com.example.demo.dto.coupon.CouponDto;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.entity.store.Store;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/coupons")
@Tag(name = "매장 쿠폰 관리", description = "매장 관리자용 쿠폰 관리 API")
public class StoreCouponManagementController {

    private final CouponService couponService;

    private ResponseEntity<?> checkStoreAuthorization(AppUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증이 필요합니다.");
        }
        if (!(user instanceof Store)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("매장 주인만 접근 가능합니다.");
        }
        return null; // Store 사용자 인증 통과
    }

    @Operation(summary = "매장 쿠폰 생성", description = "로그인한 매장 주인이 자신의 매장에 쿠폰을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createCoupon(
            @Valid @RequestBody CouponCreateRequestDto couponCreateRequestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkStoreAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        Store store = (Store) user; // 인증된 사용자는 Store 인스턴스임이 보장됨

        try {
            CouponDto createdCoupon = couponService.createCoupon(couponCreateRequestDto, store.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 생성 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "매장 쿠폰 수정", description = "로그인한 매장 주인이 자신의 매장의 특정 쿠폰을 수정합니다.")
    @PutMapping("/{couponId}")
    public ResponseEntity<?> updateCoupon(
            @Parameter(description = "수정할 쿠폰의 ID") @PathVariable Long couponId,
            @Valid @RequestBody CouponCreateRequestDto couponUpdateRequestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkStoreAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        Store store = (Store) user;

        try {
            CouponDto updatedCoupon = couponService.updateCoupon(couponId, store.getId(), couponUpdateRequestDto);
            return ResponseEntity.ok(updatedCoupon);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 로깅 추가 고려
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 수정 중 오류가 발생했습니다.");
        }
    }
}