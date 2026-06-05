package com.tuan.ecommerce.modules.payment.application;

import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.PaymentMethod;
import com.tuan.ecommerce.modules.payment.domain.Payment;
import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;
import com.tuan.ecommerce.modules.payment.infrastructure.persistence.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final long DEFAULT_PAYMENT_EXPIRY_MINUTES = 30;

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(Order order, PaymentMethod method) {
        PaymentStatus initialStatus = method == PaymentMethod.COD
                ? PaymentStatus.COD_PENDING
                : PaymentStatus.PENDING;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(method)
                .status(initialStatus)
                .expiresAt(method == PaymentMethod.ONLINE
                        ? LocalDateTime.now().plusMinutes(DEFAULT_PAYMENT_EXPIRY_MINUTES)
                        : null)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Transactional
    public Payment updatePaymentStatus(Payment payment, PaymentStatus status, String providerRef) {
        payment.setStatus(status);
        if (providerRef != null && !providerRef.isBlank()) {
            payment.setProviderRef(providerRef.trim());
        }
        if (status == PaymentStatus.PAID || status == PaymentStatus.COD_COLLECTED) {
            payment.setPaidAt(LocalDateTime.now());
        }
        return paymentRepository.save(payment);
    }
}

