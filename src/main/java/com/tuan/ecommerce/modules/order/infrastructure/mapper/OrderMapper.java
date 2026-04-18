package com.tuan.ecommerce.modules.order.infrastructure.mapper;

import com.tuan.ecommerce.modules.order.application.dto.OrderItemResponse;
import com.tuan.ecommerce.modules.order.application.dto.OrderResponse;
import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .userId(order.getUser().getId())
                .userName(order.getUser().getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
