package com.tuan.ecommerce.modules.product.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    private String description;
    
    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotEmpty(message = "Product must have at least one SKU")
    private List<SkuRequest> skus;

    private List<String> imageUrls;
}
