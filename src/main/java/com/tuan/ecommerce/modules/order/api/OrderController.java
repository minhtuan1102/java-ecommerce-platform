package com.tuan.ecommerce.modules.order.api;

import com.tuan.ecommerce.modules.order.application.OrderService;
import com.tuan.ecommerce.modules.order.application.dto.CheckoutRequest;
import com.tuan.ecommerce.modules.order.application.dto.OrderResponse;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
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
    public ResponseEntity<List<OrderResponse>> checkout(@Valid @RequestBody CheckoutRequest request, Principal principal) {
        List<OrderResponse> orders = orderService.checkout(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orders);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getName()));
    }

    @GetMapping("/shop-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderResponse>> getShopOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getShopOrders(principal.getName()));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, principal.getName()));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
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
}
