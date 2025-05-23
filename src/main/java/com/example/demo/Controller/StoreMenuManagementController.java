package com.example.demo.Controller;

import com.example.demo.Service.StoreMenuService;
import com.example.demo.dto.MenuRequestDto;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.Service.Amazon.S3UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
@Tag(name = "매장 메뉴 관리", description = "매장 관리자용 메뉴 관리 API")
public class StoreMenuManagementController {
    private final StoreMenuService storeMenuService;
    private final S3UploadService s3UploadService;

    @Operation(summary = "메뉴 목록 조회", description = "매장의 모든 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenus(
            @Parameter(description = "매장 ID") @PathVariable Long storeId) {
        List<MenuResponseDto> menuList = storeMenuService.getAllMenus(storeId);
        return ResponseEntity.ok(menuList);
    }

    @Operation(summary = "메뉴 등록", description = "매장에 새로운 메뉴를 등록합니다.")
    @PostMapping(value = "/{storeId}/menus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MenuResponseDto> createMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 이름") @RequestParam("menuName") String menuName,
            @Parameter(description = "가격") @RequestParam("price") int price,
            @Parameter(description = "메뉴 설명") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "판매 가능 여부") @RequestParam(value = "available", defaultValue = "true") boolean available,
            @Parameter(description = "카테고리") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "메뉴 이미지") @RequestParam(value = "image", required = false) MultipartFile image) {

        // 이미지 파일이 제공된 경우 S3에 업로드
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3UploadService.uploadFile(image);
        }

        // DTO 생성
        MenuRequestDto menuRequestDto = new MenuRequestDto();
        menuRequestDto.setMenuName(menuName);
        menuRequestDto.setPrice(price);
        menuRequestDto.setDescription(description);
        menuRequestDto.setImageUrl(imageUrl);
        menuRequestDto.setAvailable(available);
        menuRequestDto.setCategory(category);

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
    @PutMapping(value = "/{storeId}/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MenuResponseDto> updateMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @Parameter(description = "메뉴 이름") @RequestParam("menuName") String menuName,
            @Parameter(description = "가격") @RequestParam("price") int price,
            @Parameter(description = "메뉴 설명") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "판매 가능 여부") @RequestParam(value = "available", defaultValue = "true") boolean available,
            @Parameter(description = "카테고리") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "메뉴 이미지") @RequestParam(value = "image", required = false) MultipartFile image) {

        // DTO 생성
        MenuRequestDto menuRequestDto = new MenuRequestDto();
        menuRequestDto.setMenuName(menuName);
        menuRequestDto.setPrice(price);
        menuRequestDto.setDescription(description);
        menuRequestDto.setAvailable(available);
        menuRequestDto.setCategory(category);

        // 이미지 파일이 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(image);
            menuRequestDto.setImageUrl(imageUrl);
        }

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
