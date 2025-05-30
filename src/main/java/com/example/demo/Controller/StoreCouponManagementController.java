package com.example.demo.Controller;

import com.example.demo.Service.coupon.CouponService;
import com.example.demo.dto.coupon.CouponCreateRequestDto;
import com.example.demo.dto.coupon.CouponDto;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.entity.store.Store;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Operation(
            summary = "매장 쿠폰 생성",
            description = "로그인한 매장 주인이 자신의 매장에 쿠폰을 생성합니다. 요청 본문은 `application/json` 형식이며, `CouponCreateRequestDto`의 구조를 따릅니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 쿠폰의 상세 정보입니다. `CouponCreateRequestDto` 스키마를 참조하세요.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponCreateRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "절대 만료 방식 예시",
                                            summary = "특정 날짜에 만료되는 쿠폰 생성",
                                            value = "{\n" +
                                                    "  \"couponName\": \"가을맞이 특별 할인\",\n" +
                                                    "  \"discountType\": \"PERCENTAGE\",\n" +
                                                    "  \"discountValue\": 10,\n" +
                                                    "  \"discountLimit\": 5000,\n" +
                                                    "  \"minimumOrderAmount\": 20000,\n" +
                                                    "  \"expiryType\": \"ABSOLUTE\",\n" +
                                                    "  \"expiryDate\": \"2024-12-31T23:59:59\",\n" +
                                                    "  \"issueStartTime\": \"2023-10-26T10:00:00\",\n" +
                                                    "  \"totalQuantity\": 100,\n" +
                                                    "  \"applicableCategories\": [\"음료\", \"디저트\"]\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "상대 만료 방식 예시",
                                            summary = "발급일로부터 특정 기간 후 만료되는 쿠폰 생성",
                                            value = "{\n" +
                                                    "  \"couponName\": \"첫 구매 감사 쿠폰\",\n" +
                                                    "  \"discountType\": \"AMOUNT\",\n" +
                                                    "  \"discountValue\": 3000,\n" +
                                                    "  \"minimumOrderAmount\": 15000,\n" +
                                                    "  \"expiryType\": \"RELATIVE\",\n" +
                                                    "  \"expiryDays\": 30,\n" +
                                                    "  \"issueStartTime\": \"2023-11-01T00:00:00\",\n" +
                                                    "  \"totalQuantity\": 500\n" +
                                                    "}"
                                    )
                            }
                    )
            )
    )
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

    @Operation(
            summary = "매장 쿠폰 수정",
            description = "로그인한 매장 주인이 자신의 매장의 특정 쿠폰을 수정합니다. 요청 본문은 `application/json` 형식이며, `CouponCreateRequestDto`의 구조를 따릅니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 쿠폰의 상세 정보입니다. `CouponCreateRequestDto` 스키마를 참조하세요.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponCreateRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "절대 만료 방식으로 수정 예시",
                                            summary = "쿠폰 정보를 특정 날짜 만료로 수정",
                                            value = "{\n" +
                                                    "  \"couponName\": \"수정된 가을맞이 특별 할인\",\n" +
                                                    "  \"discountType\": \"PERCENTAGE\",\n" +
                                                    "  \"discountValue\": 15,\n" +
                                                    "  \"discountLimit\": 6000,\n" +
                                                    "  \"minimumOrderAmount\": 25000,\n" +
                                                    "  \"expiryType\": \"ABSOLUTE\",\n" +
                                                    "  \"expiryDate\": \"2025-01-31T23:59:59\",\n" +
                                                    "  \"issueStartTime\": \"2023-10-27T10:00:00\",\n" +
                                                    "  \"totalQuantity\": 150,\n" +
                                                    "  \"applicableCategories\": [\"음료\"]\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "상대 만료 방식으로 수정 예시",
                                            summary = "쿠폰 정보를 발급일로부터 특정 기간 만료로 수정",
                                            value = "{\n" +
                                                    "  \"couponName\": \"수정된 첫 구매 감사 쿠폰\",\n" +
                                                    "  \"discountType\": \"AMOUNT\",\n" +
                                                    "  \"discountValue\": 3500,\n" +
                                                    "  \"minimumOrderAmount\": 18000,\n" +
                                                    "  \"expiryType\": \"RELATIVE\",\n" +
                                                    "  \"expiryDays\": 45,\n" +
                                                    "  \"issueStartTime\": \"2023-11-02T00:00:00\",\n" +
                                                    "  \"totalQuantity\": 550\n" +
                                                    "}"
                                    )
                            }
                    )
            )
    )
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