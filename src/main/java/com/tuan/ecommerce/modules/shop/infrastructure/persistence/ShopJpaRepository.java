package com.tuan.ecommerce.modules.shop.infrastructure.persistence;

import com.tuan.ecommerce.modules.shop.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopJpaRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwnerId(Long ownerId);
    boolean existsByNameIgnoreCase(String name);
}
