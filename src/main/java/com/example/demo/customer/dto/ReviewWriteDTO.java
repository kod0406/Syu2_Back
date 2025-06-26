package com.example.demo.customer.dto;

import com.example.demo.customer.entity.CustomerReviewCollect;
import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.customer.entity.Customer;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreMenu;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
@Slf4j
public class ReviewWriteDTO {
    private Long statisticsId;
    private LocalDate date;
    private String comment;
    private Double reviewRating;
    private String imageUrl;
    public CustomerReviewCollect toEntity(Customer customer, Store store, CustomerStatistics customerStatistics, StoreMenu storeMenu) {

        CustomerReviewCollect customerReviewCollect = CustomerReviewCollect.builder()
                        .score(this.reviewRating)
                        .reviewDetails(this.comment)
                        .reviewDate(this.date)
                        .customer(customer)
                        .imageUrl(this.imageUrl)
                        .storeMenu(storeMenu)
                        .store(store)
                        .build();
        return customerReviewCollect;
    }

}
