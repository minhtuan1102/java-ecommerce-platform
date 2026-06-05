package com.tuan.ecommerce.modules.brand.infrastructure.persistence;

import com.tuan.ecommerce.modules.brand.domain.Brand;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BrandRepositoryAdpater implements BrandRepository {
    private final BrandJpaRepository brandJpaRepository;

    public BrandRepositoryAdpater(BrandJpaRepository brandJpaRepository) {
        this.brandJpaRepository = brandJpaRepository;
    }
    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(brand);
    }

    @Override
    public List<Brand> findAll() {
        return brandJpaRepository.findAll();
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return brandJpaRepository.findById(id);
    }

    @Override
    public Optional<Brand> findBySlug(String slug) {
        return brandJpaRepository.findBySlug(slug);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return brandJpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return brandJpaRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public void delete(Brand brand) {
        brandJpaRepository.delete(brand);
    }

    @Override
    public Optional<Brand> findByName(String name) {
        return brandJpaRepository.findByName(name);
    }
}
