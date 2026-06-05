package com.tuan.ecommerce.modules.order.infrastructure.persistence;

import com.tuan.ecommerce.modules.order.domain.OrderStatusHistory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderStatusHistoryRepositoryAdapter implements OrderStatusHistoryRepository {

    private final OrderStatusHistoryJpaRepository jpaRepository;

    public OrderStatusHistoryRepositoryAdapter(OrderStatusHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderStatusHistory save(OrderStatusHistory history) {
        return jpaRepository.save(history);
    }

    @Override
    public List<OrderStatusHistory> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}

