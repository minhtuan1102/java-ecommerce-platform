package com.tuan.ecommerce.modules.dashboard.application.dto;

import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal deliveredRevenue;
    private long totalOrders;
    private Map<OrderStatus, Long> ordersByStatus;
}
