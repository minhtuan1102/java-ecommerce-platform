package com.tuan.ecommerce.modules.product.infrastructure.mapper;

import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import com.tuan.ecommerce.modules.product.domain.ProductSpec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        return Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
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

        List<ProductResponse.SpecResponse> specs = product.getSpecs() != null ? product.getSpecs().stream()
                .map(spec -> ProductResponse.SpecResponse.builder()
                        .key(spec.getSpecKey())
                        .value(spec.getSpecValue())
                        .build())
                .collect(Collectors.toList()) : null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .active(product.isActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .skus(skus)
                .imageUrls(imageUrls)
                .specs(specs)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
