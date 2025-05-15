package com.example.demo.Controller;

import com.example.demo.Service.StoreMenuService;
import com.example.demo.dto.MenuRequestDto;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.entity.store.QR_Code;
import com.example.demo.repository.QRCodeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
@Tag(name = "매장 메뉴 관리", description = "매장 관리자용 메뉴 관리 API")
public class StoreMenuManagementController {
    private final StoreMenuService storeMenuService;

    @Operation(summary = "메뉴 목록 조회", description = "매장의 모든 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenus(
            @Parameter(description = "매장 ID") @PathVariable Long storeId) {
        List<MenuResponseDto> menuList = storeMenuService.getAllMenus(storeId);
        return ResponseEntity.ok(menuList);
    }

    @Operation(summary = "메뉴 등록", description = "매장에 새로운 메뉴를 등록합니다.")
    @PostMapping("/{storeId}/menus")
    public ResponseEntity<MenuResponseDto> createMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 정보") @RequestBody MenuRequestDto menuRequestDto) {
        MenuResponseDto newMenu = storeMenuService.createMenu(storeId, menuRequestDto);
        return ResponseEntity.ok(newMenu);
    }

    @Operation(summary = "특정 메뉴 조회", description = "매장의 특정 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<MenuResponseDto> getMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "메뉴 수정", description = "매장의 특정 메뉴를 수정합니다.")
    @PutMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<MenuResponseDto> updateMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @Parameter(description = "수정할 메뉴 정보") @RequestBody MenuRequestDto menuRequestDto) {
        MenuResponseDto updatedMenu = storeMenuService.updateMenu(storeId, menuId, menuRequestDto);
        return ResponseEntity.ok(updatedMenu);
    }

    @Operation(summary = "메뉴 삭제", description = "매장의 특정 메뉴를 삭제합니다.")
    @DeleteMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId) {
        storeMenuService.deleteMenu(storeId, menuId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리별 메뉴 조회", description = "매장의 특정 카테고리 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/category/{category}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenuByCategory(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "카테고리명") @PathVariable String category) {
        List<MenuResponseDto> menuList = storeMenuService.getMenusByCategory(storeId, category);
        return ResponseEntity.ok(menuList);
    }

    @Operation(summary = "전체 카테고리 목록 조회", description = "매장에 등록된 모든 카테고리를 조회합니다.")
    @GetMapping("/{storeId}/categories")
    public ResponseEntity<List<String>> getAllCategories(
            @Parameter(description = "매장 ID") @PathVariable Long storeId){
        List<String> categories = storeMenuService.getAllCategories(storeId);
        return ResponseEntity.ok(categories);
    }
}
