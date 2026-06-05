package com.tuan.ecommerce.modules.order.infrastructure.persistence;

import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    List<Order> findAllByOrderByCreatedAtDescIdDesc();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT FUNCTION('date', o.createdAt), SUM(o.totalAmount) " +
            "FROM Order o WHERE o.status = :status " +
            "GROUP BY FUNCTION('date', o.createdAt) " +
            "ORDER BY FUNCTION('date', o.createdAt)")
    List<Object[]> revenueByDay(@Param("status") OrderStatus status);

    @Query("SELECT oi.sku.product.id, oi.productName, SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
            "FROM OrderItem oi WHERE oi.order.status = :status " +
            "GROUP BY oi.sku.product.id, oi.productName " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> topProducts(@Param("status") OrderStatus status, Pageable pageable);
}
