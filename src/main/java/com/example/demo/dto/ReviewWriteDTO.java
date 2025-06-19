package com.example.demo.dto;

import com.example.demo.Service.Amazon.S3UploadService;
import com.example.demo.entity.common.CustomerReviewCollect;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.store.Store;
import com.example.demo.entity.store.StoreMenu;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.CustomerStatisticsRepository;
import com.example.demo.repository.StoreRepository;
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
    private LocalDate date;
    private String comment;
    private Double reviewRating;
    private MultipartFile images;
    public CustomerReviewCollect toEntity(Customer customer, Store store, CustomerStatistics customerStatistics, StoreMenu storeMenu, String url) {

        CustomerReviewCollect customerReviewCollect = CustomerReviewCollect.builder()
                        .score(this.reviewRating)
                        .reviewDetails(this.comment)
                        .reviewDate(this.date)
                        .imageUrl(url)
                        .customer(customer)
                        .storeMenu(storeMenu)
                        .store(store)
                        .build();
        return customerReviewCollect;
    }

}
