package com.example.demo.store.service;

import com.example.demo.setting.exception.BusinessException;
import com.example.demo.setting.exception.ErrorCode;
import com.example.demo.setting.util.S3UploadService;
import com.example.demo.store.dto.MenuRequestDto;
import com.example.demo.store.dto.MenuResponseDto;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.store.repository.QRCodeRepository;
import com.example.demo.store.repository.StoreMenuRepository;
import com.example.demo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreMenuService {
    private final StoreRepository storeRepository;
    private final StoreMenuRepository storeMenuRepository;
    private final QRCodeRepository qrCodeRepository;
    private final StoreService storeService;
    private final S3UploadService s3UploadService;

    // 메뉴 생성
    @Transactional
    public MenuResponseDto createMenu(Long storeId, MenuRequestDto menuRequestDto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        StoreMenu storeMenu = StoreMenu.builder()
                .menuName(menuRequestDto.getMenuName())
                .price(menuRequestDto.getPrice())
                .description(menuRequestDto.getDescription())
                .imageUrl(menuRequestDto.getImageUrl())
                .available(menuRequestDto.isAvailable())
                .category(menuRequestDto.getCategory())
                .rating(0.0)
                .dailySales(0)
                .revenue(0L)
                .store(store)
                .build();
        storeMenuRepository.save(storeMenu);

        return new MenuResponseDto(
                storeMenu.getMenuId(),
                storeMenu.getStore().getId(),
                storeMenu.getMenuName(),
                storeMenu.getPrice(),
                storeMenu.getRating(),
                storeMenu.getDescription(),
                storeMenu.getImageUrl(),
                storeMenu.getAvailable(),
                storeMenu.getCategory()
        );
    }

    // 메뉴 수정
    @Transactional
    public MenuResponseDto updateMenu(Long storeId, Long menuId, MenuRequestDto menuRequestDto) {
        StoreMenu storeMenu = storeMenuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_MENU_EXCEPTION));

        if (storeMenu.getStore().getStoreId() != storeId) {
            throw new BusinessException(ErrorCode.STORE_MENU_EXCEPTION);
        }

        String oldImageUrl = storeMenu.getImageUrl();
        String newImageUrl = menuRequestDto.getImageUrl();

        // 기존 이미지가 있고, 새 이미지로 교체되는 경우 기존 이미지 삭제
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            // 새 이미지가 있고 기존과 다른 경우 OR 새 이미지가 없는 경우(이미지 제거)
            if ((newImageUrl != null && !newImageUrl.equals(oldImageUrl)) ||
                    (newImageUrl == null || newImageUrl.isEmpty())) {
                s3UploadService.deleteFile(oldImageUrl);
            }
        }

        storeMenu.updateMenu(
                menuRequestDto.getMenuName(),
                menuRequestDto.getPrice(),
                menuRequestDto.getDescription(),
                menuRequestDto.getImageUrl(),
                menuRequestDto.isAvailable(),
                menuRequestDto.getCategory()
        );

        return new MenuResponseDto(
                storeMenu.getMenuId(),
                storeMenu.getStore().getId(),
                storeMenu.getMenuName(),
                storeMenu.getPrice(),
                storeMenu.getRating(),
                storeMenu.getDescription(),
                storeMenu.getImageUrl(),
                storeMenu.getAvailable(),
                storeMenu.getCategory()
        );
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(Long storeId, Long menuId) {
        StoreMenu storeMenu = storeMenuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_MENU_EXCEPTION));

        if (storeMenu.getStore().getStoreId() != storeId) {
            throw new BusinessException(ErrorCode.STORE_MENU_EXCEPTION);
        }

        if (storeMenu.getImageUrl() != null && !storeMenu.getImageUrl().isEmpty()) {
            s3UploadService.deleteFile(storeMenu.getImageUrl());
        }

        storeMenuRepository.delete(storeMenu);
    }

    // 모든 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponseDto> getAllMenus(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        return storeMenuRepository.findByStore(store).stream()
                .map(menu -> new MenuResponseDto(
                        menu.getMenuId(),
                        menu.getStore().getId(),
                        menu.getMenuName(),
                        menu.getPrice(),
                        menu.getRating(),
                        menu.getDescription(),
                        menu.getImageUrl(),
                        menu.getAvailable(),
                        menu.getCategory()
                ))
                .collect(Collectors.toList());
    }

    // 카테고리별 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponseDto> getMenusByCategory(Long storeId, String category) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        return storeMenuRepository.findByStoreAndCategory(store, category).stream()
                .map(menu -> new MenuResponseDto(
                        menu.getMenuId(),
                        menu.getStore().getId(),
                        menu.getMenuName(),
                        menu.getPrice(),
                        menu.getRating(),
                        menu.getDescription(),
                        menu.getImageUrl(),
                        menu.getAvailable(),
                        menu.getCategory()
                ))
                .collect(Collectors.toList());
    }

    // 매장의 모든 카테고리 조회
    @Transactional(readOnly = true)
    public List<String> getAllCategories(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        return storeMenuRepository.findCategoriesByStore(store);
    }

    @Transactional
    public MenuResponseDto toggleMenuAvailability(Long storeId, Long menuId) {
        StoreMenu storeMenu = storeMenuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_MENU_EXCEPTION));

        if (storeMenu.getStore().getStoreId() != storeId) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        boolean newAvailability = !storeMenu.getAvailable();
        storeMenu.updateMenu(
                storeMenu.getMenuName(),
                storeMenu.getPrice(),
                storeMenu.getDescription(),
                storeMenu.getImageUrl(),
                newAvailability,
                storeMenu.getCategory()
        );

        return new MenuResponseDto(
                storeMenu.getMenuId(),
                storeMenu.getStore().getId(),
                storeMenu.getMenuName(),
                storeMenu.getPrice(),
                storeMenu.getRating(),
                storeMenu.getDescription(),
                storeMenu.getImageUrl(),
                newAvailability,
                storeMenu.getCategory()
        );
    }
}