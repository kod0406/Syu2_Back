package com.example.demo.customer.service;

import com.example.demo.store.dto.MenuResponseDto;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.store.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerMenuService {
    private final MenuRepository menuRepository;
    //파라미터 들어가야함.
    public List<MenuResponseDto> getMenus(Long storeId) {
        List<StoreMenu> storeMenus = menuRepository.findAllByStore_StoreId(storeId);

        return storeMenus.stream()
                .map(menu -> new MenuResponseDto(
                        menu.getMenuId(),
                        menu.getStore().getStoreId(),
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

}
