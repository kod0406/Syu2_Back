package com.example.demo.dto;

import lombok.Data;

@Data
public class MenuRequestDto {
    private String menuName;
    private int price;
    private String description;
    private String imageUrl;
    private boolean available;
}
