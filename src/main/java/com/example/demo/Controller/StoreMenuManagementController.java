package com.example.demo.Controller;

import com.example.demo.Service.StoreMenuService;
import com.example.demo.dto.MenuRequestDto;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.Service.Amazon.S3UploadService;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private ResponseEntity<?> checkAuthorization(AppUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증이 필요합니다.");
        }

        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            if (!"local".equals(customer.getProvider())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Local 사용자만 접근 가능합니다.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("권한이 없습니다.");
        }

        return null; // 인증 통과
    }

    @Operation(summary = "메뉴 목록 조회", description = "매장의 모든 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<?> getMenus(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        List<MenuResponseDto> menuList = storeMenuService.getAllMenus(storeId);
        return ResponseEntity.ok(menuList);
    }

    @Operation(summary = "메뉴 등록", description = "매장에 새로운 메뉴를 등록합니다.")
    @PostMapping(value = "/{storeId}/menus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMenu(
            @PathVariable Long storeId,
            @ModelAttribute MenuRequestDto menuRequestDto,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        // 이미지 파일이 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(image);
            menuRequestDto.setImageUrl(imageUrl);
        }

        MenuResponseDto newMenu = storeMenuService.createMenu(storeId, menuRequestDto);
        return ResponseEntity.ok(newMenu);
    }

    // 나머지 메서드들도 동일하게 @AuthenticationPrincipal 추가 및 인증 검사 적용
    @Operation(summary = "특정 메뉴 조회", description = "매장의 특정 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<?> getMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "메뉴 수정", description = "매장의 특정 메뉴를 수정합니다.")
    @PutMapping(value = "/{storeId}/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @ModelAttribute MenuRequestDto menuRequestDto,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

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
    public ResponseEntity<?> deleteMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        storeMenuService.deleteMenu(storeId, menuId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리별 메뉴 조회", description = "매장의 특정 카테고리 메뉴를 조회합니다.")
    @GetMapping("/{storeId}/category/{category}/menus")
    public ResponseEntity<?> getMenuByCategory(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "카테고리명") @PathVariable String category,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        List<MenuResponseDto> menuList = storeMenuService.getMenusByCategory(storeId, category);
        return ResponseEntity.ok(menuList);
    }

    @Operation(summary = "전체 카테고리 목록 조회", description = "매장에 등록된 모든 카테고리를 조회합니다.")
    @GetMapping("/{storeId}/categories")
    public ResponseEntity<?> getAllCategories(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        List<String> categories = storeMenuService.getAllCategories(storeId);
        return ResponseEntity.ok(categories);
    }
}