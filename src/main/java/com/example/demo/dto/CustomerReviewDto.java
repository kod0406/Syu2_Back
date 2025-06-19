package com.example.demo.dto;

import com.example.demo.entity.common.CustomerReviewCollect;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerReviewDto {
    private String customerName;
    private String comment;
    private Double rating;
    private String imageUrl;
    private LocalDate reviewDate;

    public static CustomerReviewDto fromEntity(CustomerReviewCollect review) {
        CustomerReviewDto dto = new CustomerReviewDto();
        dto.customerName = review.getCustomer().getNickName(); // 연관 객체 접근
        dto.comment = review.getReviewDetails();
        dto.rating = review.getScore();
        dto.imageUrl = review.getImageUrl();
        dto.reviewDate = review.getReviewDate();
        return dto;
    }
}
