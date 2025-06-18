package com.example.demo.Controller;

import com.example.demo.entity.store.Store;
import com.example.demo.Service.StoreMenuService;
import com.example.demo.dto.MenuRequestDto;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.Service.Amazon.S3UploadService;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.dto.MenuSalesResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

        if (user instanceof Store) {
            return null;
        } else if (user instanceof Customer) {
            Customer customer = (Customer) user;
            if (!"local".equals(customer.getProvider())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Local 사용자만 접근 가능합니다.");
            }
            return null;
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("권한이 없습니다.");
        }
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

    @Operation(
            summary = "메뉴 등록",
            description = "매장에 새로운 메뉴를 등록합니다. 메뉴 정보와 이미지는 `multipart/form-data` 형식으로 전달합니다. " +
                    "메뉴 정보의 각 필드는 `MenuRequestDto` 스키마를 따릅니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MenuRequestDto.class)
                            // Swagger UI는 MenuRequestDto의 필드들을 form 파라미터로 보여주고,
                            // 각 필드 설명은 MenuRequestDto 내의 @Schema 어노테이션을 따릅니다.
                            // 'image' 파트는 아래 @RequestParam과 @Parameter로 설명됩니다.
                    )
            )
    )
    @PostMapping(value = "/{storeId}/menus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMenu(
            @PathVariable Long storeId,
            @ModelAttribute MenuRequestDto menuRequestDto, // 이 DTO의 필드들이 form-data 파라미터가 됩니다.
            @Parameter(description = "메뉴 이미지 파일 (선택 사항)") // Swagger UI에서 파일 업로드 UI를 제공합니다.
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(image);
            menuRequestDto.setImageUrl(imageUrl);
        }

        MenuResponseDto newMenu = storeMenuService.createMenu(storeId, menuRequestDto);
        return ResponseEntity.ok(newMenu);
    }

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
        // 실제 메뉴 조회 로직이 필요합니다. 현재는 OK만 반환합니다.
        // 예: MenuResponseDto menu = storeMenuService.getMenuById(storeId, menuId);
        // return ResponseEntity.ok(menu);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "메뉴 수정",
            description = "매장의 특정 메뉴를 수정합니다. 메뉴 정보와 이미지는 `multipart/form-data` 형식으로 전달합니다. " +
                    "메뉴 정보의 각 필드는 `MenuRequestDto` 스키마를 따릅니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MenuRequestDto.class)
                    )
            )
    )
    @PutMapping(value = "/{storeId}/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMenu(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @ModelAttribute MenuRequestDto menuRequestDto,
            @Parameter(description = "메뉴 이미지 파일 (변경 시에만 첨부, 선택 사항)")
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

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

    @Operation(summary = "메뉴 주문 가능 상태 변경", description = "메뉴의 주문 가능 상태를 토글합니다.")
    @PatchMapping("/{storeId}/menus/{menuId}/availability")
    public ResponseEntity<?> toggleMenuAvailability(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        MenuResponseDto updatedMenu = storeMenuService.toggleMenuAvailability(storeId, menuId);
        return ResponseEntity.ok(updatedMenu);
    }

    @Operation(summary = "특정 메뉴 판매 정보 조회", description = "특정 메뉴의 일일/전체 판매량 및 매출을 조회합니다.")
    @GetMapping("/{storeId}/menus/{menuId}/sales")
    public ResponseEntity<?> getMenuSales(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @AuthenticationPrincipal AppUser user) {

        ResponseEntity<?> authResponse = checkAuthorization(user);
        if (authResponse != null) {
            return authResponse;
        }

        MenuSalesResponseDto menuSales = storeMenuService.getMenuSales(storeId, menuId);
        return ResponseEntity.ok(menuSales);
    }
}