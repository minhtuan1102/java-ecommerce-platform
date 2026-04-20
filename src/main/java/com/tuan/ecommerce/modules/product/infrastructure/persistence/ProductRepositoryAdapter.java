package com.tuan.ecommerce.modules.product.infrastructure.persistence;

import com.tuan.ecommerce.modules.product.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public List<Product> findByShopId(Long shopId) {
        return productJpaRepository.findByShopId(shopId);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productJpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> searchProducts(String name, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        return productJpaRepository.searchProducts(name, categoryId, minPrice, maxPrice);
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.delete(product);
    }
}
