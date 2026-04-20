package com.tuan.ecommerce.modules.product.infrastructure.persistence;

import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(Long shopId);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByApprovalStatusAndActiveTrue(ProductApprovalStatus approvalStatus);
    List<Product> findByShopIdAndApprovalStatusAndActiveTrue(Long shopId, ProductApprovalStatus approvalStatus);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.skus s " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE CONCAT('%', LOWER(CAST(:name as string)), '%')) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR s.price <= :maxPrice) " +
           "AND p.active = true " +
           "AND p.approvalStatus = com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus.APPROVED")
    List<Product> searchProducts(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN p.skus s " +
            "WHERE (:name IS NULL OR LOWER(p.name) LIKE CONCAT('%', LOWER(CAST(:name as string)), '%')) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.price <= :maxPrice) " +
            "AND p.active = true " +
            "AND p.approvalStatus = com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus.APPROVED",
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM Product p LEFT JOIN p.skus s " +
                    "WHERE (:name IS NULL OR LOWER(p.name) LIKE CONCAT('%', LOWER(CAST(:name as string)), '%')) " +
                    "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
                    "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR s.price <= :maxPrice) " +
                    "AND p.active = true " +
                    "AND p.approvalStatus = com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus.APPROVED")
    Page<Product> searchProductsPage(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
