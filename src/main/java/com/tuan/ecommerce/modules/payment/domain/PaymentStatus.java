package com.tuan.ecommerce.modules.payment.domain;

public enum PaymentStatus {
    UNPAID,
    PENDING,
    PAID,
    FAILED,
    EXPIRED,
    REFUND_PENDING,
    REFUNDED,
    COD_PENDING,
    COD_COLLECTED
}

