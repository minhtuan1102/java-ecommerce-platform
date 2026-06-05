package com.tuan.ecommerce.modules.brand.infrastructure.persistence;

import com.tuan.ecommerce.modules.brand.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandJpaRepository extends JpaRepository<Brand, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Brand> findBySlug(String slug);

    Optional<Brand> findByName(String name);
}
