package com.tuan.ecommerce.modules.category.infrastructure.persistence;

import com.tuan.ecommerce.modules.category.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNull();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    void delete(Category category);

    long count();

    List<Category> saveAll(List<Category> categories);
}
