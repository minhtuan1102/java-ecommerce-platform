package com.tuan.ecommerce.modules.order.api;

import com.tuan.ecommerce.modules.order.application.OrderService;
import com.tuan.ecommerce.modules.order.application.dto.CheckoutRequest;
import com.tuan.ecommerce.modules.order.application.dto.OrderResponse;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                  Principal principal,
                                                  HttpServletRequest httpRequest) {
        OrderResponse order = orderService.checkout(principal.getName(), request, getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getAllOrders(principal.getName()));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, principal.getName()));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Principal principal) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, principal.getName()));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> cancelMyOrder(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.cancelMyOrder(orderId, principal.getName()));
    }

    @PostMapping("/{orderId}/payment/retry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> retryPayment(
            @PathVariable Long orderId,
            Principal principal,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(orderService.retryPayment(orderId, principal.getName(), getClientIp(httpRequest)));
    }

    @PatchMapping("/{orderId}/payment/refunded")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> markPaymentRefunded(
            @PathVariable Long orderId,
            @RequestParam(required = false) String providerRef,
            Principal principal) {
        return ResponseEntity.ok(orderService.markPaymentRefunded(orderId, providerRef, principal.getName()));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
