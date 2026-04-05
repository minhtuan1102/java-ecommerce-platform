package com.tuan.ecommerce.modules.category.application;

import com.tuan.ecommerce.modules.category.application.dto.CreateCategoryRequest;
import com.tuan.ecommerce.modules.category.application.dto.UpdateCategoryRequest;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.mapper.CategoryMapper;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryServiceTest {

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(new InMemoryCategoryRepository(), new CategoryMapper());
    }

    @Test
    void createCategory_shouldTrimNameAndDescription() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("  Electronics  ");
        request.setDescription("  Devices and gadgets  ");

        var result = categoryService.createCategory(request);

        assertEquals("Electronics", result.getName());
        assertEquals("Devices and gadgets", result.getDescription());
    }

    @Test
    void createCategory_shouldThrowConflict_whenNameAlreadyExists() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Fashion");

        categoryService.createCategory(request);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> categoryService.createCategory(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateCategory_shouldThrowNotFound_whenMissingCategory() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Books");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> categoryService.updateCategory(99L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private static class InMemoryCategoryRepository implements CategoryRepository {

        private final List<Category> store = new ArrayList<>();
        private final AtomicLong sequence = new AtomicLong(1L);

        @Override
        public Category save(Category category) {
            if (category.getId() == null) {
                category.setId(sequence.getAndIncrement());
                LocalDateTime now = LocalDateTime.now();
                category.setCreatedAt(now);
                category.setUpdatedAt(now);
                store.add(category);
                return category;
            }

            category.setUpdatedAt(LocalDateTime.now());
            return category;
        }

        @Override
        public List<Category> findAll() {
            return store.stream()
                    .sorted(Comparator.comparing(Category::getId))
                    .toList();
        }

        @Override
        public Optional<Category> findById(Long id) {
            return store.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public boolean existsByNameIgnoreCase(String name) {
            return store.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
        }

        @Override
        public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
            return store.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name) && !c.getId().equals(id));
        }

        @Override
        public void delete(Category category) {
            store.removeIf(c -> c.getId().equals(category.getId()));
        }
    }
}

