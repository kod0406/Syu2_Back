package com.example.demo.Controller;

import com.example.demo.Service.CustomerMenuService;
import com.example.demo.dto.MenuResponseDto;
import com.example.demo.entity.store.Store;
import com.example.demo.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "메뉴판", description = "QR코드로 접근하는 메뉴판 페이지")
public class CustomerMenuViewController {

    private final CustomerMenuService customerMenuService;
    private final StoreRepository storeRepository;

    @Operation(summary = "메뉴판 페이지", description = "QR코드로 접근하는 매장 메뉴판 페이지")
    @GetMapping("/menu/{storeId}")
    public String showMenuPage(
            @Parameter(description = "매장 ID") @PathVariable Long storeId,
            Model model) {

        // 매장 정보 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        // 메뉴 목록 조회
        List<MenuResponseDto> menuList = customerMenuService.getMenus(storeId);

        // 뷰에 데이터 전달
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("storeId", storeId);
        model.addAttribute("menuList", menuList);

        return "menu-view-test";
    }
}