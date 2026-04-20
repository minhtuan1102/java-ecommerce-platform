package com.tuan.ecommerce.modules.product.infrastructure.persistence;

import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductSkuRepositoryAdapter implements ProductSkuRepository {

    private final ProductSkuJpaRepository jpaRepository;

    public ProductSkuRepositoryAdapter(ProductSkuJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductSKU save(ProductSKU sku) {
        return jpaRepository.save(sku);
    }

    @Override
    public Optional<ProductSKU> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductSKU> findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id);
    }
}
