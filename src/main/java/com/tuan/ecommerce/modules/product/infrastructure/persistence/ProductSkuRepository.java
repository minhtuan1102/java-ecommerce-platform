package com.tuan.ecommerce.modules.product.infrastructure.persistence;

import com.tuan.ecommerce.modules.product.domain.ProductSKU;

import java.util.Optional;

public interface ProductSkuRepository {
    ProductSKU save(ProductSKU sku);
    Optional<ProductSKU> findById(Long id);
    Optional<ProductSKU> findByIdWithLock(Long id);
}
