package com.example.demo.setting.webSock;

import com.example.demo.order.dto.OrderGroupBatchMessage;
import com.example.demo.order.entity.OrderGroup;
import com.example.demo.order.repository.OrderGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebBroadCast {

        private final OrderGroupRepository orderGroupRepository;

        //실시간 주문 상황에 대한 브로드캐스트
        public OrderGroupBatchMessage createInactiveOrderGroupMessage(Long storeId) {
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

            return OrderGroupBatchMessage.builder()
                    .storeId(storeId.toString())
                    .groups(groupEntries)
                    .build();
        }
    }
