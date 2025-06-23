package com.example.demo.customer.dto;

import com.example.demo.customer.entity.CustomerReviewCollect;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerReviewDto {
    /**
     * ReviewDTO 주요 필드 요약
     *
     * ✅ 필수
     * ⛔ 선택/부가 정보
     *
     * customerName   ✅  리뷰 작성자 이름
     * comment        ✅  리뷰 내용
     * rating         ✅  별점 (예: 4.5)
     * imageUrl       ⛔  리뷰 이미지 URL (이미지가 있을 경우)
     * reviewDate     ✅  리뷰 작성 날짜
     */

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
