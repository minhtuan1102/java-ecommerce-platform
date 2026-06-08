package com.tuan.ecommerce.modules.payment.application;

import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import com.tuan.ecommerce.modules.order.domain.OrderStatusHistory;
import com.tuan.ecommerce.modules.order.domain.PaymentMethod;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderRepository;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderStatusHistoryRepository;
import com.tuan.ecommerce.modules.inventory.application.InventoryService;
import com.tuan.ecommerce.modules.payment.domain.Payment;
import com.tuan.ecommerce.modules.payment.domain.PaymentStatus;
import com.tuan.ecommerce.modules.payment.infrastructure.persistence.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final long DEFAULT_PAYMENT_EXPIRY_MINUTES = 30;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final VnpayService vnpayService;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          InventoryService inventoryService,
                          OrderStatusHistoryRepository statusHistoryRepository,
                          VnpayService vnpayService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.statusHistoryRepository = statusHistoryRepository;
        this.vnpayService = vnpayService;
    }

    @Transactional
    public Payment createPayment(Order order, PaymentMethod method) {
        PaymentStatus initialStatus = isOnlinePayment(method)
                ? PaymentStatus.PENDING
                : PaymentStatus.COD_PENDING;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(method)
                .status(initialStatus)
                .provider(isOnlinePayment(method) ? "VNPAY" : null)
                .expiresAt(isOnlinePayment(method)
                        ? LocalDateTime.now().plusMinutes(DEFAULT_PAYMENT_EXPIRY_MINUTES)
                        : null)
                .build();

        return paymentRepository.save(payment);
    }

    public boolean isOnlinePayment(PaymentMethod method) {
        return method != null && method != PaymentMethod.COD;
    }

    public String createVnpayPaymentUrl(Order order, String clientIp) {
        return vnpayService.createPaymentUrl(order, clientIp);
    }

    @Transactional
    public String retryVnpayPayment(Order order, String clientIp) {
        Payment payment = getPaymentByOrderId(order.getId());
        if (!isOnlinePayment(payment.getPaymentMethod())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only online payments can be retried");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not waiting for payment");
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is already completed");
        }
        if (payment.getStatus() == PaymentStatus.REFUND_PENDING || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is in refund flow");
        }
        if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            expirePayment(payment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment expired. Please create a new order");
        }

        payment.setStatus(PaymentStatus.PENDING);
        payment.setProviderRef(null);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(DEFAULT_PAYMENT_EXPIRY_MINUTES));
        paymentRepository.save(payment);
        return createVnpayPaymentUrl(order, clientIp);
    }

    @Transactional
    public void markRefundPendingIfPaid(Order order, String changedBy) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null || payment.getStatus() != PaymentStatus.PAID || !isOnlinePayment(payment.getPaymentMethod())) {
            return;
        }
        payment.setStatus(PaymentStatus.REFUND_PENDING);
        paymentRepository.save(payment);
        recordStatusHistory(order, order.getStatus(), changedBy, "Paid order cancelled; refund pending");
    }

    @Transactional
    public void handleOrderCancellation(Order order, String changedBy) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.PAID && isOnlinePayment(payment.getPaymentMethod())) {
            payment.setStatus(PaymentStatus.REFUND_PENDING);
            paymentRepository.save(payment);
            recordStatusHistory(order, order.getStatus(), changedBy, "Paid order cancelled; refund pending");
            return;
        }

        if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.FAILED) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public Payment markRefunded(Long orderId, String providerRef) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment.getStatus() != PaymentStatus.REFUND_PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is not waiting for refund confirmation");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        if (providerRef != null && !providerRef.isBlank()) {
            payment.setProviderRef(providerRef.trim());
        }
        return paymentRepository.save(payment);
    }

    @Scheduled(fixedDelayString = "${payment.expiry-scan-ms:60000}")
    @Transactional
    public void expirePendingPayments() {
        paymentRepository.findExpiredPendingPayments(LocalDateTime.now()).forEach(this::expirePayment);
    }

    @Transactional
    public Map<String, String> handleVnpayIpn(Map<String, String> params) {
        if (!vnpayService.isSignatureValid(params)) {
            return ipnResponse("97", "Invalid checksum");
        }

        Long orderId;
        try {
            orderId = parseOrderId(params.get("vnp_TxnRef"));
        } catch (ResponseStatusException ex) {
            return ipnResponse("01", "Order not found");
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ipnResponse("01", "Order not found");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            return ipnResponse("01", "Payment not found");
        }

        BigDecimal requestedAmount;
        try {
            requestedAmount = vnpayService.parseVnpayAmount(params);
        } catch (RuntimeException ex) {
            return ipnResponse("04", "Invalid amount");
        }

        if (requestedAmount.compareTo(order.getTotalAmount()) != 0) {
            return ipnResponse("04", "Invalid amount");
        }

        if (payment.getStatus() == PaymentStatus.PAID
                || payment.getStatus() == PaymentStatus.REFUND_PENDING
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            return ipnResponse("02", "Order already confirmed");
        }

        String providerRef = params.getOrDefault("vnp_TransactionNo", params.get("vnp_BankTranNo"));
        if (vnpayService.isSuccessfulPayment(params)) {
            updatePaymentStatus(payment, PaymentStatus.PAID, providerRef);
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                order.setStatus(OrderStatus.PENDING);
                orderRepository.save(order);
                recordStatusHistory(order, OrderStatus.PENDING, "VNPAY", "Payment completed");
            }
        } else {
            updatePaymentStatus(payment, PaymentStatus.FAILED, providerRef);
            recordStatusHistory(order, order.getStatus(), "VNPAY", "Payment failed; waiting for retry until expiry");
        }

        return ipnResponse("00", "Confirm Success");
    }

    public String buildVnpayReturnUrl(Map<String, String> params) {
        boolean validSignature = vnpayService.isSignatureValid(params);
        return vnpayService.buildFrontendReturnUrl(params, validSignature);
    }

    private Long parseOrderId(String txnRef) {
        try {
            return Long.valueOf(txnRef);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VNPAY order reference");
        }
    }

    private Map<String, String> ipnResponse(String code, String message) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("RspCode", code);
        response.put("Message", message);
        return response;
    }

    private void recordStatusHistory(Order order, OrderStatus status, String changedBy, String note) {
        statusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .changedBy(changedBy)
                .note(note)
                .build());
    }

    private void expirePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.FAILED) {
            return;
        }

        Order order = payment.getOrder();
        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            inventoryService.releaseReservations(order.getId());
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            recordStatusHistory(order, OrderStatus.CANCELLED, "SYSTEM", "Payment expired");
        }
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

