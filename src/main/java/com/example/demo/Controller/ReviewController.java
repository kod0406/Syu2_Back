package com.example.demo.Controller;

import com.example.demo.Service.ReviewService;
import com.example.demo.dto.ReviewWriteDTO;
import com.example.demo.dto.UnreviewedStatisticsDto;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰 관리", description = "고객 리뷰 작성 및 조회 API")
public class ReviewController {
    private final CustomerRepository customerRepository;
    private final ReviewService reviewService;

    @Operation(summary = "작성하지 않은 리뷰 목록 조회", description = "로그인한 고객이 아직 리뷰를 작성하지 않은 주문/통계 목록을 조회합니다.")
    @GetMapping("/review/ListShow")
    public ResponseEntity<?> reviewList(
            @Parameter(hidden = true) @AuthenticationPrincipal Customer user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<UnreviewedStatisticsDto> reviewList = reviewService.getUnreviewedStatisticsByCustomer(customer);
        return ResponseEntity.ok(reviewList);

    }

    @Operation(
            summary = "리뷰 작성",
            description = "고객이 특정 주문/통계에 대해 리뷰를 작성합니다. 리뷰 내용은 `multipart/form-data` 형식으로 전달되며, `ReviewWriteDTO`의 필드들을 따릅니다.",
            requestBody = @RequestBody(
                    description = "작성할 리뷰의 상세 정보입니다. `ReviewWriteDTO` 스키마를 참조하세요. 각 필드는 form-data의 파라미터로 전달됩니다.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ReviewWriteDTO.class)
                    )
            )
    )
    @PostMapping("/review/write")
    public ResponseEntity<?> writeReview(
            @Parameter(hidden = true) @AuthenticationPrincipal Customer user,
            @ModelAttribute ReviewWriteDTO reviewWriteDTO) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        reviewService.saveReview(customer, reviewWriteDTO);
        return ResponseEntity.ok().build();
    }

}