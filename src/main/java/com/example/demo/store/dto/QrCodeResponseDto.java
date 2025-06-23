package com.example.demo.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QrCodeResponseDto {
    private String qrCodeImage;
    private String url;
}