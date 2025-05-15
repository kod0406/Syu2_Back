package com.example.demo.dto;

import lombok.*;

@Data
public class StoreRegistrationDTO {
    private Long storeId;
    private String storeName;
    private String ownerEmail;
    private String password; //추후 추가
}
