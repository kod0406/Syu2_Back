package com.example.demo.customer.controller;

import com.example.demo.benefit.service.CustomerCouponService;
import com.example.demo.benefit.dto.CouponDto;
import com.example.demo.benefit.dto.CustomerCouponDto;
import com.example.demo.customer.entity.Customer;
import com.example.demo.setting.exception.UnauthorizedException;
import com.example.demo.setting.util.MemberValidUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
@Tag(name = "고객 쿠폰 기능", description = "고객용 쿠폰 관련 API")
public class CustomerCouponController {

    private final CustomerCouponService customerCouponService;
    private final MemberValidUtil memberValidUtil;

    @Operation(summary = "특정 가게의 발급 가능한 쿠폰 목록 조회", description = "고객이 특정 가게에서 발급받을 수 있는 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class),
                            examples = @ExampleObject(value = "[{\n" +
                                    "  \"id\": 1,\n" +
                                    "  \"couponName\": \"가을맞이 10% 할인\",\n" +
                                    "  \"discountType\": \"PERCENTAGE\",\n" +
                                    "  \"discountValue\": 10,\n" +
                                    "  \"discountLimit\": 5000,\n" +
                                    "  \"minimumOrderAmount\": 10000,\n" +
                                    "  \"expiryType\": \"ABSOLUTE\",\n" +
                                    "  \"expiryDate\": \"2024-11-30T23:59:59\",\n" +
                                    "  \"expiryDays\": null,\n" +
                                    "  \"issueStartTime\": \"2023-10-01T00:00:00\",\n" +
                                    "  \"totalQuantity\": 1000,\n" +
                                    "  \"issuedQuantity\": 150,\n" +
                                    "  \"applicableCategories\": [\"커피\", \"베이커리\"],\n" +
                                    "  \"storeId\": 101,\n" +
                                    "  \"storeName\": \"메가커피\",\n" +
                                    "  \"status\": \"ACTIVE\"\n" +
                                    "}, {\n" +
                                    "  \"id\": 2,\n" +
                                    "  \"couponName\": \"신규 고객 3000원 할인\",\n" +
                                    "  \"discountType\": \"FIXED_AMOUNT\",\n" +
                                    "  \"discountValue\": 3000,\n" +
                                    "  \"discountLimit\": null,\n" +
                                    "  \"minimumOrderAmount\": 15000,\n" +
                                    "  \"expiryType\": \"RELATIVE\",\n" +
                                    "  \"expiryDate\": null,\n" +
                                    "  \"expiryDays\": 30,\n" +
                                    "  \"issueStartTime\": \"2023-11-01T00:00:00\",\n" +
                                    "  \"totalQuantity\": 500,\n" +
                                    "  \"issuedQuantity\": 25,\n" +
                                    "  \"applicableCategories\": [],\n" +
                                    "  \"storeId\": 101,\n" +
                                    "  \"storeName\": \"메가커피\",\n" +
                                    "  \"status\": \"ACTIVE\"\n" +
                                    "}]"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(examples = @ExampleObject(value = "상점을 찾을 수 없습니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(examples = @ExampleObject(value = "쿠폰 목록 조회 중 오류가 발생했습니다.")))
    })@GetMapping("/customer/{storeId}/coupons")
    public ResponseEntity<?> getAvailableCoupons(@Parameter(description = "조회할 상점의 ID", required = true, example = "1") @PathVariable Long storeId, @AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        List<CouponDto> availableCoupons = customerCouponService.getAvailableCoupons(storeId);
        return ResponseEntity.ok(availableCoupons);
    }

    @Operation(summary = "발급 가능한 모든 쿠폰 목록 조회", description = "고객이 발급받을 수 있는 모든 쿠폰 목록을 조회합니다. 쿠폰 ID와 가게 이름이 함께 반환됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 쿠폰 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class),
                            examples = @ExampleObject(value = "[{\n" +
                                    "  \"id\": 1,\n" +
                                    "  \"couponName\": \"가을맞이 10% 할인\",\n" +
                                    "  \"discountType\": \"PERCENTAGE\",\n" +
                                    "  \"discountValue\": 10,\n" +
                                    "  \"discountLimit\": 5000,\n" +
                                    "  \"minimumOrderAmount\": 10000,\n" +
                                    "  \"expiryType\": \"ABSOLUTE\",\n" +
                                    "  \"expiryDate\": \"2024-11-30T23:59:59\",\n" +
                                    "  \"expiryDays\": null,\n" +
                                    "  \"issueStartTime\": \"2023-10-01T00:00:00\",\n" +
                                    "  \"totalQuantity\": 1000,\n" +
                                    "  \"issuedQuantity\": 150,\n" +
                                    "  \"applicableCategories\": [\"커피\", \"베이커리\"],\n" +
                                    "  \"storeId\": 101,\n" +
                                    "  \"storeName\": \"메가커피\",\n" +
                                    "  \"status\": \"ACTIVE\"\n" +
                                    "}]"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(examples = @ExampleObject(value = "전체 쿠폰 목록 조회 중 오류가 발생했습니다.")))
    })
    @GetMapping("/coupons/available")
    public ResponseEntity<?> getAllAvailableCoupons(@AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        List<CouponDto> availableCoupons = customerCouponService.getAllAvailableCoupons();
        return ResponseEntity.ok(availableCoupons);
    }

    @Operation(summary = "쿠폰 발급받기", description = "고객이 쿠폰을 발급받습니다. 로그인이 필요하며, 고객(Customer) 권한이 있어야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 발급 성공", content = @Content(examples = @ExampleObject(value = "쿠폰이 성공적으로 발급되었습니다."))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(examples = {
                    @ExampleObject(name = "재고 소진", value = "쿠폰이 모두 소진되었습니다."),
                    @ExampleObject(name = "이미 발급", value = "이미 발급받은 쿠폰입니다."),
                    @ExampleObject(name = "발급 불가 상태", value = "현재 발급 가능한 쿠폰이 아닙니다."),
                    @ExampleObject(name = "잘못된 쿠폰", value = "쿠폰을 찾을 수 없습니다.")
            })),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(examples = @ExampleObject(value = "로그인이 필요합니다."))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(examples = @ExampleObject(value = "고객만 접근 가능합니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(examples = @ExampleObject(value = "쿠폰 발급 중 오류가 발생했습니다.")))
    })
    //@SecurityRequirement(name = "bearer-key")
    @PostMapping("/coupons/{couponId}/issue")
    public ResponseEntity<?> issueCoupon(@Parameter(description = "발급받을 쿠폰의 ID", required = true, example = "1") @PathVariable Long couponId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        customerCouponService.issueCoupon(customer.getId(), couponId);
        return ResponseEntity.ok("쿠폰이 성공적으로 발급되었습니다.");
    }

    @Operation(summary = "내가 보유한 쿠폰 목록 조회", description = "고객이 보유한 쿠폰 목록 전체를 조회합니다. 로그인이 필요하며, 고객(Customer) 권한이 있어야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 쿠폰 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class),
                            examples = @ExampleObject(value = "[{\n" +
                                    "  \"id\": 1,\n" +
                                    "  \"couponName\": \"가을맞이 10% 할인\",\n" +
                                    "  \"discountType\": \"PERCENTAGE\",\n" +
                                    "  \"discountValue\": 10,\n" +
                                    "  \"minimumOrderAmount\": 10000,\n" +
                                    "  \"issuedAt\": \"2023-11-19T15:00:00\",\n" +
                                    "  \"expiresAt\": \"2025-11-30T23:59:59\",\n" +
                                    "  \"isUsed\": false,\n" +
                                    "  \"storeName\": \"메가커피\"\n" +
                                    "}, {\n" +
                                    "  \"id\": 2,\n" +
                                    "  \"couponName\": \"신규 고객 3000원 할인\",\n" +
                                    "  \"discountType\": \"FIXED_AMOUNT\",\n" +
                                    "  \"discountValue\": 3000,\n" +
                                    "  \"minimumOrderAmount\": 15000,\n" +
                                    "  \"issuedAt\": \"2023-11-01T10:00:00\",\n" +
                                    "  \"expiresAt\": \"2025-12-01T10:00:00\",\n" +
                                    "  \"isUsed\": true,\n" +
                                    "  \"storeName\": \"컴포즈커피\"\n" +
                                    "}]"))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(examples = @ExampleObject(value = "로그인이 필요합니다."))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(examples = @ExampleObject(value = "고객만 접근 가능합니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(examples = @ExampleObject(value = "내 쿠폰 목록 조회 중 오류가 발생했습니다.")))
    })
    @SecurityRequirement(name = "access_token")
    @GetMapping("/my-coupons")
    public ResponseEntity<?> getMyCoupons(@Parameter(hidden = true) @AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(customerCouponService.getMyCoupons(customer.getId()));
    }

    @Operation(summary = "특정 가게에서 사용 가능한 내 쿠폰 목록 조회", description = "고객이 특정 가게에서 사용할 수 있는, 보유 중인 쿠폰 목록을 조회합니다. 로그인이 필요하며, 고객(Customer) 권한이 있어야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 내 쿠폰 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerCouponDto.class),
                            examples = @ExampleObject(value = "[\n" +
                                    "  {\n" +
                                    "    \"id\": \"a1b2c3d4-e5f6-7890-1234-567890abcdef\",\n" +
                                    "    \"couponId\": 1,\n" +
                                    "    \"couponName\": \"가을맞이 10% 할인\",\n" +
                                    "    \"discountType\": \"PERCENTAGE\",\n" +
                                    "    \"discountValue\": 10,\n" +
                                    "    \"minimumOrderAmount\": 10000,\n" +
                                    "    \"issuedAt\": \"2025-06-01T10:00:00\",\n" +
                                    "    \"expiresAt\": \"2025-07-01T23:59:59\",\n" +
                                    "    \"isUsed\": false,\n" +
                                    "    \"storeName\": \"메가커피\"\n" +
                                    "  },\n" +
                                    "  {\n" +
                                    "    \"id\": \"b2c3d4e5-f6a1-8901-2345-67890bcdef12\",\n" +
                                    "    \"couponId\": 2,\n" +
                                    "    \"couponName\": \"신규 고객 3000원 할인\",\n" +
                                    "    \"discountType\": \"FIXED_AMOUNT\",\n" +
                                    "    \"discountValue\": 3000,\n" +
                                    "    \"minimumOrderAmount\": 15000,\n" +
                                    "    \"issuedAt\": \"2025-06-10T09:00:00\",\n" +
                                    "    \"expiresAt\": \"2025-07-10T23:59:59\",\n" +
                                    "    \"isUsed\": false,\n" +
                                    "    \"storeName\": \"메가커피\"\n" +
                                    "  }\n" +
                                    "]"))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "상점을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "access_token")
    @GetMapping("/my-coupons/store/{storeId}")
    public ResponseEntity<?> getMyUsableCouponsInStore(
            @Parameter(description = "조회할 상점의 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        List<CustomerCouponDto> coupons = customerCouponService.getMyUsableCouponsInStore(customer.getId(), storeId);
        return ResponseEntity.ok(coupons);
    }

    @Operation(summary = "UUID로 쿠폰 정보 조회", description = "UUID로 특정 쿠폰의 상태 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CouponDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"id\": 1,\n" +
                                    "  \"couponUuid\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                                    "  \"couponName\": \"가을맞이 10% 할인\",\n" +
                                    "  \"discountType\": \"PERCENTAGE\",\n" +
                                    "  \"discountValue\": 10,\n" +
                                    "  \"discountLimit\": 5000,\n" +
                                    "  \"minimumOrderAmount\": 10000,\n" +
                                    "  \"expiryType\": \"ABSOLUTE\",\n" +
                                    "  \"expiryDate\": \"2024-11-30T23:59:59\",\n" +
                                    "  \"expiryDays\": null,\n" +
                                    "  \"issueStartTime\": \"2023-10-01T00:00:00\",\n" +
                                    "  \"totalQuantity\": 1000,\n" +
                                    "  \"issuedQuantity\": 150,\n" +
                                    "  \"applicableCategories\": [\"커피\", \"베이커리\"],\n" +
                                    "  \"storeId\": 101,\n" +
                                    "  \"storeName\": \"메가커피\",\n" +
                                    "  \"status\": \"ACTIVE\"\n" +
                                    "}"))),
            @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음", content = @Content(examples = @ExampleObject(value = "해당 UUID의 쿠폰을 찾을 수 없습니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(examples = @ExampleObject(value = "쿠폰 조회 중 오류가 발생했습니다.")))
    })
    @GetMapping("/coupons/uuid/{couponUuid}")
    public ResponseEntity<?> getCouponByUuid(
            @Parameter(description = "조회할 쿠폰의 UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String couponUuid) {
        CustomerCouponDto coupon = customerCouponService.getCouponByUuid(couponUuid);
        return ResponseEntity.ok(coupon);

    }
}