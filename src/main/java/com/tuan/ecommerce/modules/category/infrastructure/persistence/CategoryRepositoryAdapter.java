package com.tuan.ecommerce.modules.category.infrastructure.persistence;

import com.tuan.ecommerce.modules.category.domain.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(category);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return categoryJpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return categoryJpaRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public void delete(Category category) {
        categoryJpaRepository.delete(category);
    }
}

