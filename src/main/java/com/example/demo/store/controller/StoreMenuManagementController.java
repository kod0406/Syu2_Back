package com.example.demo.store.controller;

import com.example.demo.store.entity.Store;
import com.example.demo.store.service.StoreMenuService;
import com.example.demo.store.dto.MenuRequestDto;
import com.example.demo.store.dto.MenuResponseDto;
import com.example.demo.setting.util.S3UploadService;
import com.example.demo.customer.entity.Customer;
import com.example.demo.user.entity.AppUser;
import com.example.demo.setting.util.MemberValidUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    private final MemberValidUtil memberValidUtil;

    @Operation(
            summary = "메뉴 목록 조회",
            description = "특정 매장에 등록된 모든 메뉴의 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메뉴 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class),
                                    examples = @ExampleObject(value = "[{\"menuId\": 1, \"menuName\": \"아메리카노\", \"price\": 4500, \"rating\": 4.5, \"description\": \"신선한 원두로 만든 아메리카노\", \"imageUrl\": \"http://example.com/americano.jpg\", \"available\": true, \"category\": \"커피\"}, {\"menuId\": 2, \"menuName\": \"카페라떼\", \"price\": 5000, \"rating\": 4.8, \"description\": \"부드러운 우유가 들어간 라떼\", \"imageUrl\": \"http://example.com/latte.jpg\", \"available\": true, \"category\": \"커피\"}]"))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<?> getMenus(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        List<MenuResponseDto> menuList = storeMenuService.getAllMenus(storeId);
        return ResponseEntity.ok(menuList);
    }

    @Operation(
            summary = "메뉴 등록",
            description = "매장에 새로운 메뉴를 등록합니다. 메뉴 정보와 이미지는 `multipart/form-data` 형식으로 전달합니다. " +
                    "메뉴 정보의 각 필드는 `MenuRequestDto` 스키마를 따릅니다.",
            requestBody = @RequestBody(
                    description = "등록할 메뉴의 상세 정보입니다. `MenuRequestDto` 스키마를 참조하세요.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MenuRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "메뉴 등록 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class),
                                    examples = @ExampleObject(value = "{\"menuId\": 3, \"menuName\": \"딸기 스무디\", \"price\": 6000, \"rating\": 0.0, \"description\": \"신선한 딸기로 만든 스무디\", \"imageUrl\": \"http://example.com/smoothie.jpg\", \"available\": true, \"category\": \"음료\"}"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @PostMapping(value = "/{storeId}/menus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMenu(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @ModelAttribute MenuRequestDto menuRequestDto,
            @Parameter(description = "메뉴 이미지 파일 (선택 사항)")
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(image);
            menuRequestDto.setImageUrl(imageUrl);
        }

        MenuResponseDto newMenu = storeMenuService.createMenu(storeId, menuRequestDto);
        return ResponseEntity.ok(newMenu);
    }

    @Operation(
            summary = "특정 메뉴 조회",
            description = "매장의 특정 메뉴 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메뉴 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class),
                                    examples = @ExampleObject(value = "{\"menuId\": 1, \"menuName\": \"아메리카노\", \"price\": 4500, \"rating\": 4.5, \"description\": \"신선한 원두로 만든 아메리카노\", \"imageUrl\": \"http://example.com/americano.jpg\", \"available\": true, \"category\": \"커피\"}"))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장 또는 메뉴", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<?> getMenu(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID", required = true, example = "1") @PathVariable Long menuId,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);
        // TODO: 실제 메뉴 조회 로직 구현 필요
        // 예: MenuResponseDto menu = storeMenuService.getMenuById(storeId, menuId);
        // return ResponseEntity.ok(menu);
        return ResponseEntity.ok().build(); // 현재는 임시로 OK만 반환
    }

    @Operation(
            summary = "메뉴 수정",
            description = "매장의 특정 메뉴를 수정합니다. 메뉴 정보와 이미지는 `multipart/form-data` 형식으로 전달합니다. " +
                    "메뉴 정보의 각 필드는 `MenuRequestDto` 스키마를 따릅니다.",
            requestBody = @RequestBody(
                    description = "수정할 메뉴의 상세 정보입니다. `MenuRequestDto` 스키마를 참조하세요.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MenuRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "메뉴 수정 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class),
                                    examples = @ExampleObject(value = "{\"menuId\": 1, \"menuName\": \"아메리카노\", \"price\": 4800, \"rating\": 4.5, \"description\": \"고급 원두로 만든 스페셜 아메리카노\", \"imageUrl\": \"http://example.com/new_americano.jpg\", \"available\": true, \"category\": \"커피\"}"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장 또는 메뉴", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @PutMapping(value = "/{storeId}/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMenu(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID", required = true, example = "1") @PathVariable Long menuId,
            @ModelAttribute MenuRequestDto menuRequestDto,
            @Parameter(description = "메뉴 이미지 파일 (변경 시에만 첨부, 선택 사항)")
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(image);
            menuRequestDto.setImageUrl(imageUrl);
        }

        MenuResponseDto updatedMenu = storeMenuService.updateMenu(storeId, menuId, menuRequestDto);
        return ResponseEntity.ok(updatedMenu);
    }

    @Operation(
            summary = "메뉴 삭제",
            description = "매장의 특정 메뉴를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메뉴 삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장 또는 메뉴", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @DeleteMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<?> deleteMenu(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID", required = true, example = "2") @PathVariable Long menuId,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        storeMenuService.deleteMenu(storeId, menuId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "카테고리별 메뉴 조회",
            description = "매장의 특정 카테고리에 속한 메뉴 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "카테고리별 메뉴 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class),
                                    examples = @ExampleObject(value = "[{\"menuId\": 1, \"menuName\": \"아메리카노\", \"price\": 4500, \"rating\": 4.5, \"description\": \"신선한 원두로 만든 아메리카노\", \"imageUrl\": \"http://example.com/americano.jpg\", \"available\": true, \"category\": \"커피\"}, {\"menuId\": 2, \"menuName\": \"카페라떼\", \"price\": 5000, \"rating\": 4.8, \"description\": \"부드러운 우유가 들어간 라떼\", \"imageUrl\": \"http://example.com/latte.jpg\", \"available\": true, \"category\": \"커피\"}]"))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/{storeId}/category/{category}/menus")
    public ResponseEntity<?> getMenuByCategory(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(description = "카테고리명", required = true, example = "커피") @PathVariable String category,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        List<MenuResponseDto> menuList = storeMenuService.getMenusByCategory(storeId, category);
        return ResponseEntity.ok(menuList);
    }

    @Operation(
            summary = "전체 카테고리 목록 조회",
            description = "매장에 등록된 모든 카테고리의 목록을 중복 없이 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = String.class),
                                    examples = @ExampleObject(value = "[\"커피\", \"음료\", \"디저트\"]"))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/{storeId}/categories")
    public ResponseEntity<?> getAllCategories(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        List<String> categories = storeMenuService.getAllCategories(storeId);
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "메뉴 주문 가능 상태 변경",
            description = "메뉴의 주문 가능 상태를 토글합니다 (가능 ↔ 불가능).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class),
                                    examples = @ExampleObject(value = "{\"menuId\": 1, \"menuName\": \"아메리카노\", \"price\": 4500, \"rating\": 4.5, \"description\": \"신선한 원두로 만든 아메리카노\", \"imageUrl\": \"http://example.com/americano.jpg\", \"available\": false, \"category\": \"커피\"}"))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 매장 또는 메뉴", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearer-key")
    @PatchMapping("/{storeId}/menus/{menuId}/availability")
    public ResponseEntity<?> toggleMenuAvailability(
            @Parameter(description = "매장 ID", required = true, example = "1") @PathVariable Long storeId,
            @Parameter(description = "메뉴 ID", required = true, example = "1") @PathVariable Long menuId,
            @Parameter(hidden = true) @AuthenticationPrincipal Store store) {

        memberValidUtil.validateIsStore(store);

        MenuResponseDto updatedMenu = storeMenuService.toggleMenuAvailability(storeId, menuId);
        return ResponseEntity.ok(updatedMenu);
    }
}