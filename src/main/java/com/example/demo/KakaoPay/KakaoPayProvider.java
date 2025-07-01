package com.example.demo.KakaoPay;

import com.example.demo.order.dto.OrderGroupBatchMessage;
import com.example.demo.customer.entity.CustomerStatistics;
import com.example.demo.order.entity.OrderGroup;
import com.example.demo.customer.entity.Customer;
import com.example.demo.customer.entity.CustomerPoint;
import com.example.demo.benefit.repository.CustomerPointRepository;
import com.example.demo.order.repository.OrderGroupRepository;
import com.example.demo.setting.webSock.WebBroadCast;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.example.demo.benefit.repository.CustomerCouponRepository;
import com.example.demo.customer.entity.CustomerCoupon;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayProvider {
    private final OrderGroupRepository orderGroupRepository;
    private final CustomerPointRepository customerPointRepository;
    private final CustomerCouponRepository customerCouponRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebBroadCast webBroadCast;

    @Value("${kakaopay.secretKey}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;
    //tid값 클라이언트에서 받아오게 수정
    private String tid;

    public KakaoPayResponse.RedirectUrlResponse ready(KakaoPayRequest.OrderRequest request, String userAgent) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", cid);
        parameters.put("partner_order_id", "1234567890");
        parameters.put("partner_user_id", "1234567890");
        parameters.put("item_name", request.getItemName());
        parameters.put("quantity", request.getQuantity());
        parameters.put("total_amount", request.getTotalPrice());
        parameters.put("tax_free_amount", "0");
        parameters.put("approval_url", "https://igo.ai.kr/api/v1/kakao-pay/approve?orderGroupId=" + request.getOrderGroup().getId());
        parameters.put("cancel_url", "https://igo.ai.kr/api/v1/kakao-pay/cancel");
        parameters.put("fail_url", "https://igo.ai.kr/kakao-pay/fail");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders(userAgent));
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

        ResponseEntity<KakaoPayResponse.ReadyResponse> response =
                restTemplate.postForEntity(url, entity, KakaoPayResponse.ReadyResponse.class);

        tid = Objects.requireNonNull(response.getBody()).getTid();

        // 👇 UA 기반 분기
        String redirectUrl = isMobile(userAgent)
                ? response.getBody().getNext_redirect_mobile_url()
                : response.getBody().getNext_redirect_pc_url();

        log.info("▶️ 리턴할 redirectUrl: {}", redirectUrl);
        return new KakaoPayResponse.RedirectUrlResponse(redirectUrl);
    }



    private HttpHeaders getHeaders(String userAgent) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + secretKey);
        headers.add("Content-type", "application/json");
        headers.add("User-Agent", userAgent);
        return headers;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + secretKey);
        headers.add("Content-type", "application/json");
        return headers;
    }


    @Transactional
    public KakaoPayResponse.ApproveResponse approve(String pgToken, Long orderGroupId) {
        log.info("orderGroupId는? " + orderGroupId);
        OrderGroup orderGroup = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        List<CustomerStatistics> stats = orderGroup.getCustomerStatisticsList();

        int totalAmount = stats.stream()
                .filter(stat -> !stat.getOrderDetails().startsWith("UserPointUsedOrNotUsed") && !stat.getOrderDetails().startsWith("CouponUsed:"))
                .mapToInt(stat -> (int) (stat.getOrderPrice() * stat.getOrderAmount()))
                .sum();

        Customer customer = orderGroup.getCustomer();
        log.info("customer은? " + customer);

        if (customer != null) {
            // --- 쿠폰 사용 처리 로직 ---
            stats.stream()
                .filter(stat -> stat.getOrderDetails().startsWith("CouponUsed:"))
                .findFirst()
                .ifPresent(couponStat -> {
                    String couponUuid = couponStat.getOrderDetails().substring("CouponUsed:".length()).trim();
                    log.info("사용된 쿠폰 UUID: {}", couponUuid);

                    CustomerCoupon customerCoupon = customerCouponRepository.findById(couponUuid)
                        .orElseThrow(() -> new IllegalStateException("사용된 쿠폰을 찾을 수 없습니다: " + couponUuid));

                    // ✅ 쿠폰 상태 검증
                    if (customerCoupon.getCouponStatus() != com.example.demo.benefit.entity.CouponStatus.UNUSED) {
                        throw new IllegalStateException("이미 사용되었거나 유효하지 않은 쿠폰입니다.");
                    }

                    // ✅ 쿠폰 만료 시간 검증 추가
                    if (customerCoupon.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                        throw new IllegalStateException("만료된 쿠폰입니다. 만료 시간: " + customerCoupon.getExpiresAt());
                    }

                    // ✅ 부모 쿠폰의 절대 만료 시간 검증 (Coupon 엔티티의 expiryDate)
                    if (customerCoupon.getCoupon().getExpiryDate() != null &&
                        customerCoupon.getCoupon().getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
                        throw new IllegalStateException("만료된 쿠폰입니다. 쿠폰 만료 시간: " + customerCoupon.getCoupon().getExpiryDate());
                    }

                    customerCoupon.markAsUsed();
                    customerCouponRepository.save(customerCoupon);
                    log.info("✅ 쿠폰 {} 사용 처리 완료", couponUuid);
                });

            // --- 포인트 사용 처리 로직 ---
            int pointUsed = stats.stream()
                    .filter(stat -> "UserPointUsedOrNotUsed".equals(stat.getOrderDetails()))
                    .mapToInt(stat -> (int) (Math.abs(stat.getOrderPrice()) * stat.getOrderAmount()))  // ✅ 절대값 처리
                    .sum();

            // 💡 CustomerPoint를 DB에서 먼저 조회하고, 없으면 저장
            CustomerPoint customerPoint = customerPointRepository.findByCustomer(customer)
                    .orElse(null);

            if (customerPoint == null) {
                customerPoint = CustomerPoint.builder()
                        .customer(customer)
                        .pointAmount(0L)
                        .build();
                customerPointRepository.save(customerPoint);
            }
            log.info("포인트 얼마 사용?" + pointUsed);
            if (pointUsed > 0) {
                if (customerPoint.getPointAmount() < pointUsed) {
                    throw new IllegalStateException("포인트가 부족합니다. 보유 포인트: " + customerPoint.getPointAmount());
                }
                customerPoint.subtractPoint(pointUsed);
                log.info("💸 포인트 {} 차감 완료", pointUsed);
            }

            log.info("잔여 포인트: {}", customerPoint.getPointAmount());

            int point = (int) (totalAmount * 0.01);
            customerPoint.addPoint(point);
            log.info("📈 {} 포인트 적립됨 (총 잔여 포인트: {})", point, customerPoint.getPointAmount());

            customerPointRepository.save(customerPoint); // 최종 저장
        }

        Long storeId = orderGroup.getStoreId();
        OrderGroupBatchMessage message = webBroadCast.createInactiveOrderGroupMessage(storeId);
        messagingTemplate.convertAndSend("/topic/orders/" + storeId, message);

        //웹 소켓 추가 끝
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", cid);
        parameters.put("tid", tid);
        parameters.put("partner_order_id", "1234567890");
        parameters.put("partner_user_id", "1234567890");
        parameters.put("pg_token", pgToken); // 결제승인 요청을 인증하는 토큰

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        ResponseEntity<KakaoPayResponse.ApproveResponse> response =
                restTemplate.postForEntity(url, entity, KakaoPayResponse.ApproveResponse.class);

        orderGroup.markAsApproved();

        return response.getBody();
    }

    private boolean isMobile(String userAgent) {
        return userAgent != null && userAgent.toLowerCase().matches(".*(iphone|android|mobile).*");
    }

}
