package com.example.demo.Controller;

import com.example.demo.dto.OrderGroupBatchMessage;
import com.example.demo.entity.common.OrderGroup;
import com.example.demo.repository.OrderGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderSocketController {

    private final OrderGroupRepository orderGroupRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/{orderGroupId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderGroupId) {
        OrderGroup orderGroup = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다."));

        orderGroup.markAsCompleted(); // 주문 완료 처리
        orderGroupRepository.save(orderGroup);

        // 주문 완료 후 WebSocket 갱신
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
        return ResponseEntity.ok().build();
    }
}
