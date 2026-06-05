package com.tuan.ecommerce.modules.payment.infrastructure.persistence;

import com.tuan.ecommerce.modules.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrderUserId(Long userId);
}

