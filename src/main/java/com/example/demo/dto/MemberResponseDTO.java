package com.example.demo.dto;

import com.example.demo.entity.customer.Customer;
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


    public MemberResponseDTO(Customer customer) {
        this.id = customer.getId();
        this.role = customer.getRole();
    }
}