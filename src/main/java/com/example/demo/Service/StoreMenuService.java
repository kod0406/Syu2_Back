package com.example.demo.Service;

import com.example.demo.dto.MenuRequestDto;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.entity.store.QR_Code;
import com.example.demo.entity.store.Store;
import com.example.demo.entity.store.StoreMenu;
import com.example.demo.repository.QRCodeRepository;
import com.example.demo.repository.StoreMenuRepository;
import com.example.demo.repository.StoreRepository;
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

    //메뉴 생성
    @Transactional
    public MenuResponseDto createMenu(Long storeId, MenuRequestDto menuRequestDto){
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 매장입니다."));

        StoreMenu storeMenu = StoreMenu.builder()
                .menuName(menuRequestDto.getMenuName())
                .price(menuRequestDto.getPrice())
                .description(menuRequestDto.getDescription())
                .imageUrl(menuRequestDto.getImageUrl())
                .available(menuRequestDto.isAvailable())
                .category(menuRequestDto.getCategory()) // 카테고리 필드 추가
                .rating(0.0) // 초기 평점
                .dailySales(0) // 초기 판매량
                .revenue(0L) // 초기 수익
                .store(store)
                .build();
        storeMenuRepository.save(storeMenu);

        // QR코드 생성
        //generateOrUpdateQRCode(store);

        return new MenuResponseDto(
                storeMenu.getMenuName(),
                storeMenu.getPrice(),
                storeMenu.getRating(),
                storeMenu.getDescription(),
                storeMenu.getImageUrl(),
                storeMenu.getCategory()
        );
    }

    //메뉴 수정
    @Transactional
    public MenuResponseDto updateMenu(Long storeId, Long menuId, MenuRequestDto menuRequestDto) {
        StoreMenu storeMenu = storeMenuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        // 해당 메뉴가 요청한 매장의 것인지 확인
        if (storeMenu.getStore().getStoreId() != storeId) {
            throw new IllegalArgumentException("해당 매장의 메뉴가 아닙니다.");
        }

        // 메뉴 정보 업데이트
        storeMenu.updateMenu(
                menuRequestDto.getMenuName(),
                menuRequestDto.getPrice(),
                menuRequestDto.getDescription(),
                menuRequestDto.getImageUrl(),
                menuRequestDto.isAvailable(),
                menuRequestDto.getCategory()
        );

        return new MenuResponseDto(
                storeMenu.getMenuName(),
                storeMenu.getPrice(),
                storeMenu.getRating(),
                storeMenu.getDescription(),
                storeMenu.getImageUrl(),
                storeMenu.getCategory()
        );
    }

    //메뉴 삭제
    @Transactional
    public void deleteMenu(Long storeId, Long menuId){
        StoreMenu storeMenu = storeMenuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        if(storeMenu.getStore().getStoreId() != storeId){
            throw new IllegalArgumentException("해당 매장의 메뉴가 아닙니다.");
        }
        storeMenuRepository.delete(storeMenu);
    }

    //모든 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponseDto> getAllMenus(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        return storeMenuRepository.findByStore(store).stream()
                .map(menu -> new MenuResponseDto(
                        menu.getMenuName(),
                        menu.getPrice(),
                        menu.getRating(),
                        menu.getDescription(),
                        menu.getImageUrl(),
                        menu.getCategory()
                ))
                .collect(Collectors.toList());
    }

    //카테고리별 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponseDto> getMenusByCategory(Long storeId, String category){
        Store store = storeRepository.findById(storeId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 매장입니다."));

        return storeMenuRepository.findByStoreAndCategory(store, category).stream()
                .map(menu -> new MenuResponseDto(
                        menu.getMenuName(),
                        menu.getPrice(),
                        menu.getRating(),
                        menu.getDescription(),
                        menu.getImageUrl(),
                        menu.getCategory()
                ))
                .collect(Collectors.toList());
    }
    // 매장의 모든 카테고리 조회
    @Transactional(readOnly = true)
    public List<String> getAllCategories(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        return storeMenuRepository.findCategoriesByStore(store);
    }

    /*// QR 코드 생성 또는 업데이트
    private void generateOrUpdateQRCode(Store store) {
        // 매장의 QR 코드가 있는지 확인
        QR_Code qrCode = qrCodeRepository.findByStore(store)
                .orElse(null);

        // QR 코드 URL 생성 (예: /menu/{storeId})
        String menuUrl = "/menu/" + store.getStoreId();

        if (qrCode == null) {
            // 새 QR 코드 생성
            qrCode = QR_Code.builder()
                    .QR_Code(menuUrl)  // QR_Code 클래스에 url 필드가 있어야 함
                    .store(store)
                    .build();
        } else {
            // URL만 업데이트 (QR_Code 클래스에 updateUrl 메서드가 필요함)
            qrCode.updateUrl(menuUrl);
        }

        qrCodeRepository.save(qrCode);
    }*/
}