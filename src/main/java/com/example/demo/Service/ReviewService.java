package com.example.demo.Service;

import com.example.demo.Service.Amazon.S3UploadService;
import com.example.demo.dto.ReviewWriteDTO;
import com.example.demo.dto.UnreviewedStatisticsDto;
import com.example.demo.entity.common.CustomerReviewCollect;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.store.Store;
import com.example.demo.entity.store.StoreMenu;
import com.example.demo.repository.CustomerReviewCollectRepository;
import com.example.demo.repository.CustomerStatisticsRepository;
import com.example.demo.repository.StoreMenuRepository;
import com.example.demo.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CustomerStatisticsRepository customerStatisticsRepository;
    private final S3UploadService s3UploadService;
    private final CustomerReviewCollectRepository customerReviewCollectRepository;
    private final StoreRepository storeRepository;
    private final StoreMenuRepository storeMenuRepository;

    public List<UnreviewedStatisticsDto> getUnreviewedStatisticsByCustomer(Customer customer) {
        List<CustomerStatistics> unreviewedList = customerStatisticsRepository.findByCustomerAndReviewedFalse(customer);

        return unreviewedList.stream()
                .filter(stat -> !"UserPointUsedOrNotUsed".equals(stat.getOrderDetails())) // 이 조건을 추가
                .map(stat -> new UnreviewedStatisticsDto(
                        stat.getId(),
                        stat.getStore().getStoreName(),
                        stat.getOrderDetails(),
                        stat.getOrderPrice(),
                        stat.getOrderAmount(),
                        stat.getDate()
                ))
                .toList();
    }

    @Transactional
    public void saveReview(Customer customer, ReviewWriteDTO reviewWriteDTO) {
        //TODO
        CustomerStatistics customerStatistics = customerStatisticsRepository.findById(reviewWriteDTO.getStatisticsId()).orElse(null);
        Store store = storeRepository.findById(customerStatistics.getId()).orElse(null);
        StoreMenu storeMenu = storeMenuRepository.findByMenuName(customerStatistics.getOrderDetails());

        s3UploadService.uploadFile(reviewWriteDTO.getImages());

        CustomerReviewCollect reviewCollect = reviewWriteDTO.toEntity(customer, store, customerStatistics, storeMenu);
        customerReviewCollectRepository.save(reviewCollect);
    }

}