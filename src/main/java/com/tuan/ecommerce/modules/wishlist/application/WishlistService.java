package com.tuan.ecommerce.modules.wishlist.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.infrastructure.mapper.ProductMapper;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import com.tuan.ecommerce.modules.wishlist.domain.WishlistItem;
import com.tuan.ecommerce.modules.wishlist.infrastructure.persistence.WishlistItemJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemJpaRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public WishlistService(WishlistItemJpaRepository wishlistRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           ProductMapper productMapper) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getMyWishlist(String email) {
        User user = findUser(email);
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(WishlistItem::getProduct)
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> getMyWishlistProductIds(String email) {
        User user = findUser(email);
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(item -> item.getProduct().getId())
                .toList();
    }

    @Transactional
    public List<Long> add(String email, Long productId) {
        User user = findUser(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            wishlistRepository.save(WishlistItem.builder()
                    .user(user)
                    .product(product)
                    .build());
        }

        return getMyWishlistProductIds(email);
    }

    @Transactional
    public List<Long> remove(String email, Long productId) {
        User user = findUser(email);
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return getMyWishlistProductIds(email);
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
