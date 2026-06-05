package com.tuan.ecommerce.modules.brand.infrastructure.persistence;

import com.tuan.ecommerce.modules.brand.domain.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    Brand save(Brand brand);

    List<Brand> findAll();

    Optional<Brand> findById(Long id);

    Optional<Brand> findBySlug(String slug);

    void delete(Brand brand);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Brand> findByName(String name);
}
