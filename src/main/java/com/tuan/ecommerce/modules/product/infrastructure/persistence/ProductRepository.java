package com.tuan.ecommerce.modules.product.infrastructure.persistence;

import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findByShopId(Long shopId);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByShopIdAndApprovalStatusAndActiveTrue(Long shopId, ProductApprovalStatus approvalStatus);
    List<Product> findByApprovalStatusAndActiveTrue(ProductApprovalStatus approvalStatus);
    void delete(Product product);
    List<Product> searchProducts(String name, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    Page<Product> searchProductsPage(String name, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable);
}
