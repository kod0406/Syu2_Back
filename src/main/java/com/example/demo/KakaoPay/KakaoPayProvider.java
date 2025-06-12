package com.example.demo.KakaoPay;

import com.example.demo.dto.OrderGroupBatchMessage;
import com.example.demo.entity.common.CustomerStatistics;
import com.example.demo.entity.common.OrderGroup;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.customer.CustomerPoint;
import com.example.demo.repository.CustomerPointRepository;
import com.example.demo.repository.CustomerStatisticsRepository;
import com.example.demo.repository.OrderGroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class KakaoPayProvider {
    private final OrderGroupRepository orderGroupRepository;
    private final CustomerPointRepository customerPointRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${kakaopay.secretKey}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;
    //tid값 클라이언트에서 받아오게 수정
    private String tid;

    public KakaoPayResponse.ReadyResponse ready(KakaoPayRequest.OrderRequest request) {

        Map<String, String> parameters = new HashMap<>();

        parameters.put("cid", cid); // 가맹점 코드, 테스트용은 TC0ONETIME
        parameters.put("partner_order_id", "1234567890"); // 주문번호, 임시 : 1234567890
        parameters.put("partner_user_id", "1234567890"); // 회원아이디, 임시 : 1234567890
        parameters.put("item_name", request.getItemName()); // 상품명
        parameters.put("quantity", request.getQuantity()); // 상품 수량
        parameters.put("total_amount", request.getTotalPrice()); // 상품 총액
        parameters.put("tax_free_amount", "0"); // 상품 비과세 금액
        parameters.put("approval_url", "http://localhost:8080/api/v1/kakao-pay/approve?orderGroupId=" + request.getOrderGroup().getId()); // 결제 성공 시 redirct URL
        parameters.put("cancel_url", "http://localhost:8080/api/v1/kakao-pay/cancel"); // 결제 취소 시
        parameters.put("fail_url", "http://localhost:8080/kakao-pay/fail"); // 결제 실패 시

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        ResponseEntity<KakaoPayResponse.ReadyResponse> response = restTemplate.postForEntity(url, entity, KakaoPayResponse.ReadyResponse.class);

        tid = Objects.requireNonNull(response.getBody()).getTid();
        log.info("로그: " + response.getBody().getTid());
        return response.getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + secretKey);
        headers.add("Content-type", "application/json");
        return headers;
    }

    public KakaoPayResponse.ApproveResponse approve(String pgToken, Long orderGroupId) {
        log.info("orderGroupId는? " + orderGroupId);
        OrderGroup orderGroup = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        List<CustomerStatistics> stats = orderGroup.getCustomerStatisticsList();

        int totalAmount = stats.stream()
                .mapToInt(stat -> (int) (stat.getOrderPrice() * stat.getOrderAmount()))
                .sum();

        Customer customer = orderGroup.getCustomer();
        log.info("customer은?" + customer);

        if (customer != null) {
            int pointUsed = stats.stream()
                    .filter(stat -> "UserPointUsedOrNotUsed".equals(stat.getOrderDetails()))
                    .mapToInt(stat -> (int) (stat.getOrderPrice() * stat.getOrderAmount()))
                    .sum();

            CustomerPoint customerPoint = customerPointRepository.findByCustomer(customer)
                    .orElseGet(() -> CustomerPoint.builder()
                            .customer(customer)
                            .pointAmount(0L)
                            .build());

            if (pointUsed > 0) {
                if (customerPoint.getPointAmount() < pointUsed) {
                    throw new IllegalStateException("포인트가 부족합니다. 보유 포인트: " + customerPoint.getPointAmount());
                }
                customerPoint.subtractPoint(pointUsed); // ⬅️ 차감 메서드는 엔티티에 정의해야 함
                log.info("💸 포인트 {} 차감 완료", pointUsed);
            }

            int point = (int) (totalAmount * 0.01);

            customerPoint = customerPointRepository.findByCustomer(customer)
                    .orElseGet(() -> CustomerPoint.builder()
                            .customer(customer)
                            .pointAmount(0L)
                            .build());

            customerPoint.addPoint(point);
            customerPointRepository.save(customerPoint);
        }

        // ✅ 필요 시 결제 성공 로그 추가
        log.info("✅ 결제 승인 성공: 주문번호 {}, 총금액 {}, 포인트 {} 적립됨", orderGroupId, totalAmount, (int)(totalAmount * 0.01));
        //웹 소켓 추가 부분
        Long storeId = orderGroup.getStoreId();
        List<OrderGroup> inactiveGroups = orderGroupRepository.findAllByStoreIdAndActiveFalse(storeId);
        List<OrderGroupBatchMessage.OrderGroupEntry> groupEntries = inactiveGroups.stream()
                .map(group -> {
                    List<OrderGroupBatchMessage.OrderItem> items = group.getCustomerStatisticsList().stream()
                            .map(stat -> OrderGroupBatchMessage.OrderItem.builder()
                                    .menuName(stat.getOrderDetails())
                                    .price((int) stat.getOrderPrice())
                                    .quantity((int) stat.getOrderAmount())
                                    .build())
                            .toList();

                    return OrderGroupBatchMessage.OrderGroupEntry.builder()
                            .orderGroupId(group.getId())
                            .items(items)
                            .build();
                })
                .toList();
            OrderGroupBatchMessage message = OrderGroupBatchMessage.builder()
                    .storeId(storeId.toString())
                    .groups(groupEntries)
                    .build();

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

        return response.getBody();
    }

}
