package com.tuan.ecommerce.modules.order.infrastructure.persistence;

import com.tuan.ecommerce.modules.order.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findByShopId(Long shopId);
}
