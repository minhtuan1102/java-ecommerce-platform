package com.tuan.ecommerce.modules.order.infrastructure.persistence;

import com.tuan.ecommerce.modules.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByShopId(Long shopId);
}
