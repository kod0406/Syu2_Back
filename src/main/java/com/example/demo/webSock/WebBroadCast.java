package com.example.demo.webSock;

import com.example.demo.dto.OrderGroupBatchMessage;
import com.example.demo.entity.common.OrderGroup;
import com.example.demo.repository.OrderGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebBroadCast {

        private final OrderGroupRepository orderGroupRepository;

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
