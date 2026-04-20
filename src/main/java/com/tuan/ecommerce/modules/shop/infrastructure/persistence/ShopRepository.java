package com.tuan.ecommerce.modules.shop.infrastructure.persistence;

import com.tuan.ecommerce.modules.shop.domain.Shop;

import java.util.List;
import java.util.Optional;

public interface ShopRepository {
    Shop save(Shop shop);
    Optional<Shop> findById(Long id);
    Optional<Shop> findByOwnerId(Long ownerId);
    List<Shop> findAll();
    boolean existsByNameIgnoreCase(String name);
    void delete(Shop shop);
}
