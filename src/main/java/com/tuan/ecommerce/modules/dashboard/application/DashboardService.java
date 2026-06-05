package com.tuan.ecommerce.modules.dashboard.application;

import com.tuan.ecommerce.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.tuan.ecommerce.modules.dashboard.application.dto.RevenuePointResponse;
import com.tuan.ecommerce.modules.dashboard.application.dto.TopProductResponse;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final OrderJpaRepository orderRepository;

    public DashboardService(OrderJpaRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        Arrays.stream(OrderStatus.values()).forEach(status -> byStatus.put(status, 0L));

        for (Object[] row : orderRepository.countOrdersByStatus()) {
            byStatus.put((OrderStatus) row[0], (Long) row[1]);
        }

        return DashboardSummaryResponse.builder()
                .deliveredRevenue(defaultAmount(orderRepository.sumTotalAmountByStatus(OrderStatus.DELIVERED)))
                .totalOrders(orderRepository.count())
                .ordersByStatus(byStatus)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RevenuePointResponse> getRevenueByDay() {
        return orderRepository.revenueByDay(OrderStatus.DELIVERED).stream()
                .map(row -> RevenuePointResponse.builder()
                        .date(toLocalDate(row[0]))
                        .revenue(defaultAmount((BigDecimal) row[1]))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopProducts(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 20));
        return orderRepository.topProducts(OrderStatus.DELIVERED, PageRequest.of(0, normalizedLimit)).stream()
                .map(row -> TopProductResponse.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .quantitySold(((Number) row[2]).longValue())
                        .revenue(defaultAmount((BigDecimal) row[3]))
                        .build())
                .toList();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
