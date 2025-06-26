package com.example.demo.user.controller;

import com.example.demo.customer.service.CustomerService;
import com.example.demo.store.service.StoreService;
import com.example.demo.customer.dto.CustomerStatisticsDto;
import com.example.demo.store.dto.MenuSalesStatisticsDto;
import com.example.demo.customer.entity.Customer;
import com.example.demo.store.entity.Store;
import com.example.demo.setting.util.MemberValidUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StoreService storeService;
    private final CustomerService customerService;
    private final MemberValidUtil memberValidUtil;

    @Operation(
            summary = "가게 통계 조회",
            description = "기간별(일간, 주간, 월간)로 메뉴별 판매량을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "통계 조회 성공", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MenuSalesStatisticsDto.class),
                            examples = @ExampleObject(value = "[{\"menuName\": \"아메리카노\", \"salesCount\": 10}, {\"menuName\": \"카페라떼\", \"salesCount\": 5}]"))),
                    @ApiResponse(responseCode = "401", description = "가게 인증 실패"),
                    @ApiResponse(responseCode = "400", description = "잘못된 기간 요청 (daily, weekly, monthly 중 하나)")
            }
    )
    @GetMapping("/store")
    public ResponseEntity<List<MenuSalesStatisticsDto>> storeGetStatistics(@AuthenticationPrincipal Store store,
                                                                        @Parameter(description = "기간 (daily, weekly, monthly)", required = true, examples = {
                                                                                @ExampleObject(name = "일간", value = "daily"),
                                                                                @ExampleObject(name = "주간", value = "weekly"),
                                                                                @ExampleObject(name = "월간", value = "monthly")
                                                                        }) @RequestParam String period) {
        memberValidUtil.validateIsStore(store);

        LocalDate end = LocalDate.now();
        LocalDate start;

        switch (period.toLowerCase()) {
            case "daily" -> start = end;
            case "weekly" -> start = end.with(java.time.DayOfWeek.MONDAY);
            case "monthly" -> start = end.withDayOfMonth(1);
            default -> {
                return ResponseEntity.badRequest().body(null);
            }
        }

        List<MenuSalesStatisticsDto> result = storeService.storeStatistics(store, start, end);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "고객 이용 내역 통계",
            description = "고객이 특정 가게에서 얼마나 주문했는지 메뉴별 통계를 제공합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "통계 조회 성공", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerStatisticsDto.class),
                            examples = @ExampleObject(value = "[{\"menuName\": \"아메리카노\", \"orderCount\": 5}, {\"menuName\": \"카페라떼\", \"orderCount\": 2}]"))),
                    @ApiResponse(responseCode = "401", description = "고객 인증 실패")
            }
    )
    @GetMapping("/customer")
    public ResponseEntity<List<CustomerStatisticsDto>> customerGetStatistics(@AuthenticationPrincipal Customer customer,
                                                                             @Parameter(description = "가게 이름", required = true, example = "스타벅스") @RequestParam String storeName) {
        if (!memberValidUtil.isCustomer(customer)) {
            return ResponseEntity.status(401).build();
        }
        List<CustomerStatisticsDto> result = customerService.customerStatistics(customer, storeName);

        return ResponseEntity.ok(result);

    }
}
