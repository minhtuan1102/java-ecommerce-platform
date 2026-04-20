package com.tuan.ecommerce.modules.product.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;
    private List<SkuResponse> skus;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuResponse {
        private Long id;
        private String skuCode;
        private String tierIndex;
        private BigDecimal price;
        private Integer stock;
    }
}
