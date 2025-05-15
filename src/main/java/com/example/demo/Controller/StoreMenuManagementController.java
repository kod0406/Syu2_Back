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
    private final QRCodeRepository qrCodeRepository;

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
        // 여기에 특정 메뉴 조회 로직이 필요합니다
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

    // QR 코드 정보 조회
    @GetMapping("/{storeId}/qrcode")
    public ResponseEntity<Map<String, String>> getQrCode(@PathVariable Long storeId) {
        Map<String, String> response = new HashMap<>();

        // 매장 QR 코드 정보 조회
        qrCodeRepository.findByStoreStoreId(storeId).ifPresent(qrCode -> {
            response.put("url", qrCode.getQR_Code());
            response.put("qrImageUrl", "/api/store/" + storeId + "/qrcode/image");
        });

        if (response.isEmpty()) {
            response.put("url", "/menu/" + storeId);
            response.put("qrImageUrl", "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=/menu/" + storeId);
        }

        return ResponseEntity.ok(response);
    }
}