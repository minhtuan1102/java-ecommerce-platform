package com.tuan.ecommerce.modules.order.infrastructure.persistence;

import com.tuan.ecommerce.modules.order.domain.OrderStatusHistory;

import java.util.List;

public interface OrderStatusHistoryRepository {
    OrderStatusHistory save(OrderStatusHistory history);
    List<OrderStatusHistory> findByOrderId(Long orderId);
}

