package com.tuan.ecommerce.modules.order.application.dto;

import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import com.tuan.ecommerce.modules.order.domain.PaymentMethod;
import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String recipientName;
    private String shippingAddress;
    private String phoneNumber;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentProviderRef;
    private LocalDateTime paymentPaidAt;
    private LocalDateTime paymentExpiresAt;
    private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}

