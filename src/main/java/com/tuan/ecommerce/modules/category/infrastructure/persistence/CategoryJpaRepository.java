package com.tuan.ecommerce.modules.category.infrastructure.persistence;

import com.tuan.ecommerce.modules.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNull();
}
