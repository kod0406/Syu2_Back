package com.example.demo.setting.webSock;

import com.example.demo.benefit.repository.CustomerCouponRepository;
import com.example.demo.customer.entity.CustomerCoupon;
import com.example.demo.order.dto.OrderGroupBatchMessage;
import com.example.demo.order.entity.OrderGroup;
import com.example.demo.order.repository.OrderGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.demo.benefit.entity.Coupon;

import java.util.List;

//@Component
//@RequiredArgsConstructor
//public class WebBroadCast {
//
//        private final OrderGroupRepository orderGroupRepository;
//
//        //실시간 주문 상황에 대한 브로드캐스트
//        public OrderGroupBatchMessage createInactiveOrderGroupMessage(Long storeId) {
//            List<OrderGroup> inactiveGroups = orderGroupRepository.findAllByStoreIdAndActiveFalse(storeId);
//
//            List<OrderGroupBatchMessage.OrderGroupEntry> groupEntries = inactiveGroups.stream()
//                    .map(group -> {
//                        List<OrderGroupBatchMessage.OrderItem> items = group.getCustomerStatisticsList().stream()
//                                .map(stat -> OrderGroupBatchMessage.OrderItem.builder()
//                                        .menuName(stat.getOrderDetails())
//                                        .price((int) stat.getOrderPrice())
//                                        .quantity((int) stat.getOrderAmount())
//                                        .build())
//                                .toList();
//
//                        return OrderGroupBatchMessage.OrderGroupEntry.builder()
//                                .orderGroupId(group.getId())
//                                .items(items)
//                                .build();
//                    })
//                    .toList();
//
//            return OrderGroupBatchMessage.builder()
//                    .storeId(storeId.toString())
//                    .groups(groupEntries)
//                    .build();
//        }
//    }
import com.example.demo.benefit.repository.CouponRepository;

@Component
@RequiredArgsConstructor
public class WebBroadCast {

    private final OrderGroupRepository orderGroupRepository;
    private final CouponRepository couponRepository;
    private final CustomerCouponRepository customerCouponRepository;

    public OrderGroupBatchMessage createInactiveOrderGroupMessage(Long storeId) {
        List<OrderGroup> inactiveGroups = orderGroupRepository.findAllByStoreIdAndActiveFalse(storeId);

        List<OrderGroupBatchMessage.OrderGroupEntry> groupEntries = inactiveGroups.stream()
                .map(group -> {
                    List<OrderGroupBatchMessage.OrderItem> items = group.getCustomerStatisticsList().stream()
                            .map(stat -> {
                                String menuName = convertMenuName(stat.getOrderDetails());

                                return OrderGroupBatchMessage.OrderItem.builder()
                                        .menuName(menuName)
                                        .price((int) stat.getOrderPrice())
                                        .quantity((int) stat.getOrderAmount())
                                        .build();
                            })
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

    private String convertMenuName(String raw) {
        if ("UserPointUsedOrNotUsed".equals(raw)) {
            return "포인트 사용";
        }

        if (raw.startsWith("CouponUsed:")) {
            String uuid = raw.substring("CouponUsed:".length());
            String couponName = customerCouponRepository.findCouponNameByCouponUuid(uuid);
            return couponName != null ? "[쿠폰] " + couponName : "쿠폰 사용";
        }

        return raw;
    }
}
