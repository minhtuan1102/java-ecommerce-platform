package com.tuan.ecommerce.modules.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    CANCELLED
}
