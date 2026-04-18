package com.tuan.ecommerce.modules.cart.infrastructure.persistence;

import com.tuan.ecommerce.modules.cart.domain.Cart;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    public CartRepositoryAdapter(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return cartJpaRepository.findByUserId(userId);
    }

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }
}
