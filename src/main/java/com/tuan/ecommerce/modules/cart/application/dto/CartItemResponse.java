package com.tuan.ecommerce.modules.cart.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id; // CartItem ID
    private Long skuId;
    private String productName;
    private String tierIndex;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
