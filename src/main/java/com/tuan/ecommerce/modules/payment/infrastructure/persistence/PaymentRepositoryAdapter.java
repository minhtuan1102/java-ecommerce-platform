package com.tuan.ecommerce.modules.payment.infrastructure.persistence;

import com.tuan.ecommerce.modules.payment.domain.Payment;
import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return jpaRepository.findByOrderUserId(userId);
    }

    @Override
    public List<Payment> findExpiredPendingPayments(LocalDateTime now) {
        return jpaRepository.findByStatusInAndExpiresAtBefore(List.of(PaymentStatus.PENDING, PaymentStatus.FAILED), now);
    }
}

