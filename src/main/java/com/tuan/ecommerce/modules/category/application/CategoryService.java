package com.tuan.ecommerce.modules.category.application;

import com.tuan.ecommerce.modules.category.application.dto.CategoryResponse;
import com.tuan.ecommerce.modules.category.application.dto.CreateCategoryRequest;
import com.tuan.ecommerce.modules.category.application.dto.UpdateCategoryRequest;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.mapper.CategoryMapper;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String normalizedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        String normalizedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        categoryMapper.updateEntity(existingCategory, request);
        Category savedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryRepository.delete(existingCategory);
    }
}
