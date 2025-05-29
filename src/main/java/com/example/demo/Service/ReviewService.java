package com.example.demo.Service;

import com.example.demo.dto.ReviewWriteDTO;
import com.example.demo.dto.UnreviewedStatisticsDto;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import com.example.demo.repository.CustomerStatisticsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CustomerStatisticsRepository customerStatisticsRepository;

    public List<UnreviewedStatisticsDto> getUnreviewedStatisticsByCustomer(Customer customer) {
        List<CustomerStatistics> unreviewedList = customerStatisticsRepository.findByCustomerAndReviewedFalse(customer);

        return unreviewedList.stream()
                .filter(stat -> !"UserPointUserOrNotUsed".equals(stat.getOrderDetails())) // 이 조건을 추가
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
    public void saveResume(ReviewWriteDTO reviewWriteDTO, Customer member) {
        //TODO
    }

}