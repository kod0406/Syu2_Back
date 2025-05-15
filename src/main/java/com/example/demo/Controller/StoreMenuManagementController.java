package com.example.demo.Controller;

import com.example.demo.Service.StoreMenuService;
import com.example.demo.dto.MenuRequestDto;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.entity.store.QR_Code;
import com.example.demo.repository.QRCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreMenuManagementController {
    private final StoreMenuService storeMenuService;

    // 메뉴 목록 조회
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenus(@PathVariable Long storeId) {
        List<MenuResponseDto> menuList = storeMenuService.getAllMenus(storeId);
        return ResponseEntity.ok(menuList);
    }

    // 메뉴 등록
    @PostMapping("/{storeId}/menus")
    public ResponseEntity<MenuResponseDto> createMenu(
            @PathVariable Long storeId,
            @RequestBody MenuRequestDto menuRequestDto) {
        MenuResponseDto newMenu = storeMenuService.createMenu(storeId, menuRequestDto);
        return ResponseEntity.ok(newMenu);
    }

    // 특정 메뉴 조회
    @GetMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<MenuResponseDto> getMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        // 여기에 특정 메뉴 조회 로직이 필요
        return ResponseEntity.ok().build();
    }

    // 메뉴 수정
    @PutMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<MenuResponseDto> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestBody MenuRequestDto menuRequestDto) {
        MenuResponseDto updatedMenu = storeMenuService.updateMenu(storeId, menuId, menuRequestDto);
        return ResponseEntity.ok(updatedMenu);
    }

    // 메뉴 삭제
    @DeleteMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        storeMenuService.deleteMenu(storeId, menuId);
        return ResponseEntity.ok().build();
    }

    //카테고리별 메뉴 조회
    @GetMapping("/{storeId}/category/{category}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenuByCategory(
            @PathVariable Long storeId,
            @PathVariable String category) {
        List<MenuResponseDto> menuList = storeMenuService.getMenusByCategory(storeId, category);
        return ResponseEntity.ok(menuList);
    }

    //모든 카테고리 목록 조회
    @GetMapping("/{storeId}/categories")
    public ResponseEntity<List<String>> getAllCategories(@PathVariable Long storeId){
        List<String> categories = storeMenuService.getAllCategories(storeId);
        return ResponseEntity.ok(categories);

    }

}