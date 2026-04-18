package com.tuan.ecommerce.modules.cart.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCartResponse {
    private Long shopId;
    private String shopName;
    private List<CartItemResponse> items;
    private BigDecimal shopSubtotal;
}
