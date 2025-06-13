package com.example.demo.Controller;

import com.example.demo.Service.StoreService;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MenuSalesStatisticsDto;
import com.example.demo.dto.ReviewWriteDTO;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.store.Store;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store/statistics")
public class StatisticsController {
    private final StoreService storeService;

    @Operation(
            summary = "통계 요청",
            description = "통계 요청입니다."
    )
    @GetMapping
    public ResponseEntity<List<MenuSalesStatisticsDto>> getStatistics(@AuthenticationPrincipal Store store, @RequestParam String period) {
        if (store == null) {
            return ResponseEntity.status(401).build();
        }

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


}
