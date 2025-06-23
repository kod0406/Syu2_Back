package com.example.demo.order.controller;

import com.example.demo.order.dto.OrderGroupBatchMessage;
import com.example.demo.order.entity.OrderGroup;
import com.example.demo.user.entity.AppUser;
import com.example.demo.order.repository.OrderGroupRepository;
import com.example.demo.setting.webSock.WebBroadCast;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "주문 처리 WebSocket", description = "WebSocket을 통해 실시간으로 주문 상태를 업데이트하는 API입니다.")
public class OrderSocketController {

    private final OrderGroupRepository orderGroupRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebBroadCast webBroadCast;

    @Operation(summary = "주문 완료 처리", description = "점주가 특정 주문을 완료 처리합니다. 완료된 주문은 비활성 상태로 변경되고, 해당 매장의 모든 클라이언트에게 WebSocket을 통해 업데이트가 전송됩니다. 점주로 인증해야 합니다.")
    @SecurityRequirement(name = "bearer-key")
    @PostMapping("/{orderGroupId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderGroupId) {
        OrderGroup orderGroup = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다."));

        orderGroup.markAsCompleted(); // 주문 완료 처리
        orderGroupRepository.save(orderGroup);

        //음식 완료 후 WebSocket 갱신
        Long storeId = orderGroup.getStoreId();
        OrderGroupBatchMessage message = webBroadCast.createInactiveOrderGroupMessage(storeId);
        messagingTemplate.convertAndSend("/topic/orders/" + storeId, message);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "메뉴 정보 가져오기 (WebSocket)", description = "점주가 메뉴 정보를 요청하면, 해당 매장의 비활성 주문 목록을 WebSocket을 통해 클라이언트에게 전송합니다. 점주로 인증해야 합니다.")
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/getMenu")
    public ResponseEntity<Void> getMenu(@AuthenticationPrincipal AppUser appUser) {
        Long storeId = appUser.getId();
        OrderGroupBatchMessage message = webBroadCast.createInactiveOrderGroupMessage(storeId);
        messagingTemplate.convertAndSend("/topic/orders/" + storeId, message);
        return ResponseEntity.ok().build();
    }


}
