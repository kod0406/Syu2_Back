package com.example.demo.Controller;

import com.example.demo.Service.ReviewService;
import com.example.demo.dto.ReviewWriteDTO;
import com.example.demo.dto.UnreviewedStatisticsDto;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final CustomerRepository customerRepository;
    private final ReviewService reviewService;
    @GetMapping("/review/ListShow")
    public ResponseEntity<?> reviewList(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<UnreviewedStatisticsDto> reviewList = reviewService.getUnreviewedStatisticsByCustomer(customer);
        return ResponseEntity.ok(reviewList);

    }

    @PostMapping("/review/write")
    public ResponseEntity<?> writeReview(@AuthenticationPrincipal AppUser user, @ModelAttribute ReviewWriteDTO reviewWriteDTO) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        reviewService.resumeWrite(customer, reviewWriteDTO);

    }

}
