package com.example.demo.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderGroupBatchMessage {
    private String storeId;
    private List<OrderGroupEntry> groups;

    @Data
    @Builder
    public static class OrderGroupEntry {
        private Long orderGroupId;
        private List<OrderItem> items;
    }

    @Data
    @Builder
    public static class OrderItem {
        private String menuName;
        private int price;
        private int quantity;
    }
}
