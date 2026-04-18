package com.tuan.ecommerce.modules.order.api;

import com.tuan.ecommerce.modules.order.application.OrderService;
import com.tuan.ecommerce.modules.order.application.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<OrderResponse>> checkout(Principal principal) {
        List<OrderResponse> orders = orderService.checkout(principal.getName());
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
}
