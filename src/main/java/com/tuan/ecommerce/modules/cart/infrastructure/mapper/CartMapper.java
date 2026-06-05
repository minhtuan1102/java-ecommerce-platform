package com.tuan.ecommerce.modules.cart.infrastructure.mapper;

import com.tuan.ecommerce.modules.cart.application.dto.CartItemResponse;
import com.tuan.ecommerce.modules.cart.application.dto.CartResponse;
import com.tuan.ecommerce.modules.cart.domain.Cart;
import com.tuan.ecommerce.modules.cart.domain.CartItem;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            return CartResponse.builder().cartId(cart != null ? cart.getId() : null).totalAmount(BigDecimal.ZERO).build();
        }

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getSku().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .skuId(item.getSku().getId())
                .productName(item.getSku().getProduct().getName())
                .imageUrl(getMainImageUrl(item))
                .tierIndex(item.getSku().getTierIndex())
                .price(item.getSku().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }

    private String getMainImageUrl(CartItem item) {
        List<ProductImage> images = item.getSku().getProduct().getImages();
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(ProductImage::isMain)
                .findFirst()
                .orElse(images.get(0))
                .getUrl();
    }
}
