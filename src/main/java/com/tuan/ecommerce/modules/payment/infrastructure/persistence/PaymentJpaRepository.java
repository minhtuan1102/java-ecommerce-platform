package com.tuan.ecommerce.modules.payment.infrastructure.persistence;

import com.tuan.ecommerce.modules.payment.domain.Payment;
import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrderUserId(Long userId);
    List<Payment> findByStatusInAndExpiresAtBefore(List<PaymentStatus> statuses, LocalDateTime expiresAt);
}

