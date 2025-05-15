package com.example.demo.Controller;

import com.example.demo.Service.CustomerMenuService;
import com.example.demo.dto.MenuResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class StoreController {
    private final CustomerMenuService customerMenuService;

    @GetMapping("/api/Store/Menu")
    public ResponseEntity<List<MenuResponseDto>> getMenu(@RequestParam String StoreNumber) {
        Long storeId = Long.parseLong(StoreNumber);

        List<MenuResponseDto> menuList = customerMenuService.getMenus(storeId);
        return ResponseEntity.ok(menuList);
    }
}
