package com.tuan.ecommerce.modules.cart.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.cart.application.dto.AddToCartRequest;
import com.tuan.ecommerce.modules.cart.application.dto.CartResponse;
import com.tuan.ecommerce.modules.cart.domain.Cart;
import com.tuan.ecommerce.modules.cart.domain.CartItem;
import com.tuan.ecommerce.modules.cart.infrastructure.mapper.CartMapper;
import com.tuan.ecommerce.modules.cart.infrastructure.persistence.CartRepository;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductSkuRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductSkuRepository skuRepository;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductSkuRepository skuRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.skuRepository = skuRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProductSKU sku = skuRepository.findById(request.getSkuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSku().getId().equals(sku.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .sku(sku)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long cartItemId, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart");
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }
}
