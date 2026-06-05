package com.tuan.ecommerce.modules.payment.application.dto;

import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusUpdateRequest {
    private PaymentStatus status;
    private String providerRef;
}

