package com.tuan.ecommerce.modules.payment.infrastructure.persistence;

import com.tuan.ecommerce.modules.payment.domain.Payment;
import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findExpiredPendingPayments(LocalDateTime now);
}

