package com.tuan.ecommerce.modules.product.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewProductRequest {
    @Size(max = 500, message = "Review note cannot exceed 500 characters")
    private String reviewNote;
}

