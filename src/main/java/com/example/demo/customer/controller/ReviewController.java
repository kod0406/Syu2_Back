package com.example.demo.customer.controller;

import com.example.demo.customer.service.ReviewService;
import com.example.demo.customer.dto.CustomerReviewDto;
import com.example.demo.customer.dto.ReviewWriteDTO;
import com.example.demo.customer.dto.UnreviewedStatisticsDto;
import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.setting.exception.UnauthorizedException;
import com.example.demo.setting.util.MemberValidUtil;
import com.example.demo.setting.util.S3UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰 관리", description = "고객 리뷰 작성 및 조회 API")
@RequestMapping("/api")
public class ReviewController {
    private final CustomerRepository customerRepository;
    private final ReviewService reviewService;
    private final MemberValidUtil memberValidUtil;
    private final S3UploadService s3UploadService;

    @Operation(
            summary = "작성하지 않은 리뷰 목록 조회",
            description = "로그인한 고객이 아직 리뷰를 작성하지 않은 주문/통계 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UnreviewedStatisticsDto.class),
                                    examples = @ExampleObject(value = "[{\"customerStatisticsId\": 1, \"storeName\": \"BBQ치킨\", \"menuNames\": [\"황금올리브\", \"치즈볼\"], \"orderDate\": \"2025-06-19\"}]"))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
            }
    )
    @GetMapping("/review/ListShow")
    public ResponseEntity<?> reviewList(
            @Parameter(hidden = true) @AuthenticationPrincipal Customer customer) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        List<UnreviewedStatisticsDto> reviewList = reviewService.getUnreviewedStatisticsByCustomer(customer);
        return ResponseEntity.ok(reviewList);

    }

    @Operation(
            summary = "리뷰 작성",
            description = "고객이 특정 주문/통계에 대해 리뷰를 작성합니다. 리뷰 내용은 `multipart/form-data` 형식으로 전달되며, `ReviewWriteDTO`의 필드(customerStatisticsId, rating, content)들을 따릅니다.",
            requestBody = @RequestBody(
                    description = "작성할 리뷰의 상세 정보입니다. `ReviewWriteDTO` 스키마를 참조하세요. 각 필드는 form-data의 파라미터로 전달됩니다.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ReviewWriteDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 작성 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 리뷰 내용 누락)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "관련 주문/통계 정보를 찾을 수 없음", content = @Content)
            }
    )
    @PostMapping("/review/write")
    public ResponseEntity<?> writeReview(
            @Parameter(hidden = true) @AuthenticationPrincipal Customer customer,
            @ModelAttribute ReviewWriteDTO reviewWriteDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        if (!memberValidUtil.isCustomer(customer)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(image);
            reviewWriteDTO.setImageUrl(imageUrl);
        }

        reviewService.saveReview(customer, reviewWriteDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/review/show")
    public ResponseEntity<?> showReview(@RequestParam long menuId){
        List<CustomerReviewDto> reviews = reviewService.getReviewsByMenu(menuId);
        return ResponseEntity.ok(reviews);
    }
}