package com.tuan.ecommerce.modules.product.infrastructure.persistence;

import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findByShopId(Long shopId);
    List<Product> findByShopIdAndApprovalStatusAndActiveTrue(Long shopId, ProductApprovalStatus approvalStatus);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByApprovalStatusAndActiveTrue(ProductApprovalStatus approvalStatus);
    List<Product> searchProducts(String name, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    void delete(Product product);
}
