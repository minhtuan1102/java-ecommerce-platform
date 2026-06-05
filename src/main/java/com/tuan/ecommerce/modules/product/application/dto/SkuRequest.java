package com.tuan.ecommerce.modules.product.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuRequest {
    private String skuCode;
    private String tierIndex; // ví dụ: "0,1"

    @NotNull(message = "SKU price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "SKU price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "SKU stock is required")
    @Min(value = 0, message = "SKU stock cannot be negative")
    private Integer stock;
}
