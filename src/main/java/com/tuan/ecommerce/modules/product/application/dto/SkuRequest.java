package com.tuan.ecommerce.modules.product.application.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuRequest {
    private String skuCode;
    private String tierIndex; // ví dụ: "0,1"
    private BigDecimal price;
    private Integer stock;
}
