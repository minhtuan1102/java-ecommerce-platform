package com.tuan.ecommerce.modules.cart.infrastructure.persistence;

import com.tuan.ecommerce.modules.cart.domain.Cart;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findByUserId(Long userId);
    Cart save(Cart cart);
}
