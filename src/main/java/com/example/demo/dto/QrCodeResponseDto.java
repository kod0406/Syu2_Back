package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QrCodeResponseDto {
    private String qrCodeImage;
    private String url;


    public String getQrCodeImage() {
        return qrCodeImage;
    }

    public String getUrl() {
        return url;
    }
}