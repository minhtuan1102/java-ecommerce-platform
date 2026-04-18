package com.tuan.ecommerce.modules.cart.api;

import com.tuan.ecommerce.modules.cart.application.CartService;
import com.tuan.ecommerce.modules.cart.application.dto.AddToCartRequest;
import com.tuan.ecommerce.modules.cart.application.dto.CartResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getMyCart(Principal principal) {
        return ResponseEntity.ok(cartService.getMyCart(principal.getName()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request, Principal principal) {
        return ResponseEntity.ok(cartService.addToCart(request, principal.getName()));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId, Principal principal) {
        return ResponseEntity.ok(cartService.removeItem(itemId, principal.getName()));
    }
}
