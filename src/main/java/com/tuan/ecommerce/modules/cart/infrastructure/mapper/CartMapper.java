package com.tuan.ecommerce.modules.cart.infrastructure.mapper;

import com.tuan.ecommerce.modules.cart.application.dto.CartItemResponse;
import com.tuan.ecommerce.modules.cart.application.dto.CartResponse;
import com.tuan.ecommerce.modules.cart.application.dto.ShopCartResponse;
import com.tuan.ecommerce.modules.cart.domain.Cart;
import com.tuan.ecommerce.modules.cart.domain.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            return CartResponse.builder().cartId(cart != null ? cart.getId() : null).totalAmount(BigDecimal.ZERO).build();
        }

        // Group items by Shop ID
        Map<Long, List<CartItem>> itemsByShop = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getSku().getProduct().getShop().getId()));

        List<ShopCartResponse> shopCartResponses = itemsByShop.entrySet().stream().map(entry -> {
            Long shopId = entry.getKey();
            String shopName = entry.getValue().get(0).getSku().getProduct().getShop().getName();
            
            List<CartItemResponse> itemResponses = entry.getValue().stream().map(this::toItemResponse).collect(Collectors.toList());
            
            BigDecimal shopSubtotal = itemResponses.stream()
                    .map(CartItemResponse::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return ShopCartResponse.builder()
                    .shopId(shopId)
                    .shopName(shopName)
                    .items(itemResponses)
                    .shopSubtotal(shopSubtotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal totalAmount = shopCartResponses.stream()
                .map(ShopCartResponse::getShopSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .shops(shopCartResponses)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getSku().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .skuId(item.getSku().getId())
                .productName(item.getSku().getProduct().getName())
                .tierIndex(item.getSku().getTierIndex())
                .price(item.getSku().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
