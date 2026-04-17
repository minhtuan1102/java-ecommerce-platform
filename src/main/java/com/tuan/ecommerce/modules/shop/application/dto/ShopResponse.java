package com.tuan.ecommerce.modules.shop.application.dto;

import com.tuan.ecommerce.modules.shop.domain.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private ShopStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
