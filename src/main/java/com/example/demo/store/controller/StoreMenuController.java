package com.example.demo.store.controller;

import com.example.demo.customer.service.CustomerMenuService;
import com.example.demo.store.dto.MenuResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@RestController
@Tag(name = "고객용 메뉴 조회", description = "고객이 사용하는 메뉴 조회 API")
public class StoreMenuController {
    private final CustomerMenuService customerMenuService;

    @Operation(summary = "매장 메뉴 조회", description = "지정된 매장의 모든 메뉴를 조회합니다.")
    @GetMapping("/api/Store/Menu")
    public ResponseEntity<List<MenuResponseDto>> getMenu(
            @Parameter(description = "매장 ID") @RequestParam String StoreNumber) {
        Long storeId = Long.parseLong(StoreNumber);

        List<MenuResponseDto> menuList = customerMenuService.getMenus(storeId);
        return ResponseEntity.ok(menuList);
    }
}
