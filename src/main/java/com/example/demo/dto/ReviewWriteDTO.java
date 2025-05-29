package com.example.demo.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
@Slf4j
public class ReviewWriteDTO {
    private Long statisticsId;
    private String storeName;
    private String orderDetails;
    private long orderPrice;
    private long orderAmount;
    private LocalDate date;
    private String comment;
    private Double reviewRating;
    private List<MultipartFile> images;
    private List<String> imageUrls;
}
