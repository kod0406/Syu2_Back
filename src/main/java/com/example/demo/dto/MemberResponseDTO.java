package com.example.demo.dto;

import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "공통응답 DTO")
public class MemberResponseDTO {
    private Long id;
    private String role;


    public MemberResponseDTO(AppUser user) {
        this.id = user.getId();
        this.role = user.getRole();
    }
}