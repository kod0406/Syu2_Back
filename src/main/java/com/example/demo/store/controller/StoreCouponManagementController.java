package com.example.demo.store.controller;

import com.example.demo.benefit.service.CouponService;
import com.example.demo.benefit.dto.CouponCreateRequestDto;
import com.example.demo.benefit.dto.CouponDto;
import com.example.demo.benefit.dto.CouponStatusUpdateRequestDto;
import com.example.demo.user.entity.AppUser;
import com.example.demo.store.entity.Store;
import com.example.demo.setting.util.MemberValidUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    private final MemberValidUtil memberValidUtil;

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
                                                    "  \"discountType\": \"FIXED_AMOUNT\",\n" +
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쿠폰 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(examples = @ExampleObject(value = "절대 만료 방식을 선택한 경우 만료 날짜를 입력해야 합니다."))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(examples = @ExampleObject(value = "인증이 필요합니다."))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(examples = @ExampleObject(value = "매장 주인만 접근 가능합니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "쿠폰 생성 중 오류가 발생했습니다.")))
    })
    @SecurityRequirement(name = "access_token")
    @PostMapping
    public ResponseEntity<?> createCoupon(
            @Valid @RequestBody CouponCreateRequestDto couponCreateRequestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);
        CouponDto createdCoupon = couponService.createCoupon(couponCreateRequestDto, store.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon);

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
                                                    "  \"discountType\": \"FIXED_AMOUNT\",\n" +
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(examples = @ExampleObject(value = "해당 상점에 존재하지 않는 쿠폰이거나 수정 권한이 없습니다."))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(examples = @ExampleObject(value = "인증이 필요합니다."))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(examples = @ExampleObject(value = "매장 주인만 접근 가능합니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "쿠폰 수정 중 오류가 발생했습니다.")))
    })
    @SecurityRequirement(name = "access_token")
    @PutMapping("/{couponId}")
    public ResponseEntity<?> updateCoupon(
            @Parameter(description = "수정할 쿠폰의 ID") @PathVariable Long couponId,
            @Valid @RequestBody CouponCreateRequestDto couponUpdateRequestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);
        CouponDto updatedCoupon = couponService.updateCoupon(couponId, store.getId(), couponUpdateRequestDto);
        return ResponseEntity.ok(updatedCoupon);
    }

    @Operation(
            summary = "매장 쿠폰 상태 변경",
            description = "로그인한 매장 주인이 자신의 매장의 특정 쿠폰 상태를 변경합니다. (예: ACTIVE, INACTIVE, RECALLED)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 쿠폰의 상태 정보입니다.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponStatusUpdateRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "쿠폰 활성화",
                                            summary = "쿠폰을 ACTIVE 상태로 변경",
                                            value = "{\"status\": \"ACTIVE\"}"
                                    ),
                                    @ExampleObject(
                                            name = "쿠폰 비활성화",
                                            summary = "쿠폰을 INACTIVE 상태로 변경",
                                            value = "{\"status\": \"INACTIVE\"}"
                                    ),
                                    @ExampleObject(
                                            name = "쿠폰 회수",
                                            summary = "쿠폰을 RECALLED 상태로 변경",
                                            value = "{\"status\": \"RECALLED\"}"
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 상태 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(examples = @ExampleObject(value = "해당 상점에 존재하지 않는 쿠폰이거나 수정 권한이 없습니다."))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(examples = @ExampleObject(value = "인증이 필요합니다."))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(examples = @ExampleObject(value = "매장 주인만 접근 가능합니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "쿠폰 상태 변경 중 오류가 발생했습니다.")))
    })
    @SecurityRequirement(name = "access_token")
    @PatchMapping("/{couponId}/status")
    public ResponseEntity<?> updateCouponStatus(
            @Parameter(description = "상태를 변경할 쿠폰의 ID") @PathVariable Long couponId,
            @Valid @RequestBody CouponStatusUpdateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);
        CouponDto updatedCoupon = couponService.updateCouponStatus(couponId, store.getId(), requestDto.getStatus());
        return ResponseEntity.ok(updatedCoupon);
    }

    @Operation(
            summary = "매장 쿠폰 삭제",
            description = "로그인한 매장 주인이 자신의 매장의 특정 쿠폰을 삭제합니다. 이미 고객에게 발급된 쿠폰은 삭제할 수 없습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 삭제 성공",
                    content = @Content(examples = @ExampleObject(value = "쿠폰이 성공적으로 삭제되었습니다."))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(examples = {
                            @ExampleObject(name = "존재하지 않는 쿠폰", value = "해당 상점에 존재하지 않는 쿠폰이거나 삭제 권한이 없습니다."),
                            @ExampleObject(name = "이미 발급된 쿠폰", value = "이미 고객에게 발급된 쿠폰은 삭제할 수 없습니다. 대신 상태를 변경하세요.")
                    })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(examples = @ExampleObject(value = "인증이 필요합니다."))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(examples = @ExampleObject(value = "매장 주인만 접근 가능합니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "쿠폰 삭제 중 오류가 발생했습니다.")))
    })
    @SecurityRequirement(name = "access_token")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<?> deleteCoupon(
            @Parameter(description = "삭제할 쿠폰의 ID", example = "1") @PathVariable Long couponId,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);
        couponService.deleteCoupon(couponId, store.getId());
        return ResponseEntity.ok("쿠폰이 성공적으로 삭제되었습니다.");

    }

    @Operation(
            summary = "내 매장 쿠폰 목록 조회",
            description = "로그인한 매장 주인이 자신의 매장에 대한 모든 쿠폰과 상태를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "쿠폰 목록 조회 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = CouponDto.class)),
                                    examples = @ExampleObject(value = "[\n" +
                                            "  {\n" +
                                            "    \"id\": 1,\n" +
                                            "    \"couponName\": \"여름맞이 20% 할인\",\n" +
                                            "    \"discountType\": \"PERCENTAGE\",\n" +
                                            "    \"discountValue\": 20,\n" +
                                            "    \"discountLimit\": 3000,\n" +
                                            "    \"minimumOrderAmount\": 15000,\n" +
                                            "    \"expiryType\": \"ABSOLUTE\",\n" +
                                            "    \"expiryDate\": \"2025-08-31T23:59:59\",\n" +
                                            "    \"expiryDays\": null,\n" +
                                            "    \"issueStartTime\": \"2025-06-01T00:00:00\",\n" +
                                            "    \"totalQuantity\": 500,\n" +
                                            "    \"issuedQuantity\": 120,\n" +
                                            "    \"applicableCategories\": [\"음료\", \"디저트\"],\n" +
                                            "    \"storeId\": 10,\n" +
                                            "    \"storeName\": \"카페봄\",\n" +
                                            "    \"status\": \"ACTIVE\",\n" +
                                            "    \"detail\": {\n" +
                                            "      \"couponUuid\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                                            "      \"couponCode\": \"CP12345678\"\n" +
                                            "    }\n" +
                                            "  },\n" +
                                            "  {\n" +
                                            "    \"id\": 2,\n" +
                                            "    \"couponName\": \"신규 고객 3000원 할인\",\n" +
                                            "    \"discountType\": \"FIXED_AMOUNT\",\n" +
                                            "    \"discountValue\": 3000,\n" +
                                            "    \"discountLimit\": null,\n" +
                                            "    \"minimumOrderAmount\": 20000,\n" +
                                            "    \"expiryType\": \"RELATIVE\",\n" +
                                            "    \"expiryDate\": null,\n" +
                                            "    \"expiryDays\": 30,\n" +
                                            "    \"issueStartTime\": \"2025-06-10T00:00:00\",\n" +
                                            "    \"totalQuantity\": 300,\n" +
                                            "    \"issuedQuantity\": 50,\n" +
                                            "    \"applicableCategories\": [],\n" +
                                            "    \"storeId\": 10,\n" +
                                            "    \"storeName\": \"카페봄\",\n" +
                                            "    \"status\": \"INACTIVE\",\n" +
                                            "    \"detail\": {\n" +
                                            "      \"couponUuid\": \"550e8400-e29b-41d4-a716-446655440001\",\n" +
                                            "      \"couponCode\": \"CP87654321\"\n" +
                                            "    }\n" +
                                            "  }\n" +
                                            "]")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(examples = @ExampleObject(value = "인증이 필요합니다."))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(examples = @ExampleObject(value = "매장 주인만 접근 가능합니다."))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(examples = @ExampleObject(value = "내 매장 쿠폰 목록 조회 중 오류가 발생했습니다."))
                    )
            }
    )
    @SecurityRequirement(name = "access_token")
    @GetMapping("/my")
    public ResponseEntity<?> getMyCoupons(@Parameter(hidden = true) @AuthenticationPrincipal Store store) {
        memberValidUtil.validateIsStore(store);
        return ResponseEntity.ok(couponService.getCouponsByStore(store.getId()));

    }
}