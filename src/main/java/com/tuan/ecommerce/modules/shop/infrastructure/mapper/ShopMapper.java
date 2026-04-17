package com.tuan.ecommerce.modules.shop.infrastructure.mapper;

import com.tuan.ecommerce.modules.shop.application.dto.CreateShopRequest;
import com.tuan.ecommerce.modules.shop.application.dto.ShopResponse;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShopMapper {

    public Shop toEntity(CreateShopRequest request) {
        return Shop.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();
    }

    public ShopResponse toResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .ownerId(shop.getOwner().getId())
                .status(shop.getStatus())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .build();
    }

    public List<ShopResponse> toResponseList(List<Shop> shops) {
        return shops.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
