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
    private String slug;
    private String description;
    private Long brandId;
    private String brandName;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private List<SkuResponse> skus;
    private List<String> imageUrls;
    private List<SpecResponse> specs;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecResponse {
        private String key;
        private String value;
    }
}
