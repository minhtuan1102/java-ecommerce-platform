package com.tuan.ecommerce.modules.order.infrastructure.persistence;

import com.tuan.ecommerce.modules.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
