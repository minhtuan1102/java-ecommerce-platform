package com.tuan.ecommerce.modules.product.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {

    @NotBlank(message = "Image URL is required")
    private String url;

    @Size(max = 255, message = "Cloudinary public ID cannot exceed 255 characters")
    private String cloudinaryPublicId;

    private Boolean main;

    private Integer sortOrder;
}
