package com.tuan.ecommerce.modules.payment.infrastructure.mapper;

import com.tuan.ecommerce.modules.payment.application.dto.PaymentResponse;
import com.tuan.ecommerce.modules.payment.domain.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .provider(payment.getProvider())
                .providerRef(payment.getProviderRef())
                .paidAt(payment.getPaidAt())
                .expiresAt(payment.getExpiresAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        return payments.stream().map(this::toResponse).toList();
    }
}

