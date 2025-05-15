package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuResponseDto {
    private String name;
    private int price;
    private double rating;
    private String description;
    private String imageUrl;
}