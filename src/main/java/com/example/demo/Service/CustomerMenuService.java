package com.example.demo.Service;

import com.example.demo.dto.MenuResponseDto;
import com.example.demo.entity.store.StoreMenu;
import com.example.demo.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
