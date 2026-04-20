package com.tuan.ecommerce.modules.shop.infrastructure.persistence;

import com.tuan.ecommerce.modules.shop.domain.Shop;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShopRepositoryAdapter implements ShopRepository {

    private final ShopJpaRepository shopJpaRepository;

    public ShopRepositoryAdapter(ShopJpaRepository shopJpaRepository) {
        this.shopJpaRepository = shopJpaRepository;
    }

    @Override
    public Shop save(Shop shop) {
        return shopJpaRepository.save(shop);
    }

    @Override
    public Optional<Shop> findById(Long id) {
        return shopJpaRepository.findById(id);
    }

    @Override
    public Optional<Shop> findByOwnerId(Long ownerId) {
        return shopJpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Shop> findAll() {
        return shopJpaRepository.findAll();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return shopJpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public void delete(Shop shop) {
        shopJpaRepository.delete(shop);
    }
}
