package com.example.demo.dto;

import lombok.Data;

@Data
public class MenuRequestDto {
    //DB에 저장할 때
    private String menuName;
    private int price;
    private String description;
    private String imageUrl;
    private boolean available;
    private String category;
}
