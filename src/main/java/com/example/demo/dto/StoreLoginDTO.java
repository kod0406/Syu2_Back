package com.example.demo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreLoginDTO {
    private String ownerEmail;
    private String password;
}
