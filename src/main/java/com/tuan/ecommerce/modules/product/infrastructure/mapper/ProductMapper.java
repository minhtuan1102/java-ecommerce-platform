package com.tuan.ecommerce.modules.product.infrastructure.mapper;

import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        return Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .brand(request.getBrand() != null ? request.getBrand().trim() : null)
                .active(true)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        List<ProductResponse.SkuResponse> skus = product.getSkus() != null ? product.getSkus().stream()
                .map(sku -> ProductResponse.SkuResponse.builder()
                        .id(sku.getId())
                        .skuCode(sku.getSkuCode())
                        .tierIndex(sku.getTierIndex())
                        .price(sku.getPrice())
                        .stock(sku.getStock())
                        .build())
                .collect(Collectors.toList()) : null;

        List<String> imageUrls = product.getImages() != null ? product.getImages().stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList()) : null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .active(product.isActive())
                .approvalStatus(product.getApprovalStatus() != null ? product.getApprovalStatus().name() : null)
                .reviewNote(product.getReviewNote())
                .approvedByUserId(product.getApprovedBy() != null ? product.getApprovedBy().getId() : null)
                .approvedByUsername(product.getApprovedBy() != null ? product.getApprovedBy().getUsername() : null)
                .approvedAt(product.getApprovedAt())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .shopId(product.getShop().getId())
                .shopName(product.getShop().getName())
                .skus(skus)
                .imageUrls(imageUrls)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream().map(this::toResponse).collect(Collectors.toList());
    }
}

