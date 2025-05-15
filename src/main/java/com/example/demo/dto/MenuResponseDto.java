package com.example.demo.dto;

import com.example.demo.entity.store.StoreMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
public class MenuResponseDto {
    private String menuName;
    private int price;
    private double rating;
    private String description;
    private String imageUrl;
    private String category;


}