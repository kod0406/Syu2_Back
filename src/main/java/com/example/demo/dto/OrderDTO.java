package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDTO {
    private String menuName;
    private long menuPrice;
    private long menuAmount;
    private boolean reviewed;
}
