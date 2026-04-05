package com.tuan.ecommerce.modules.category.infrastructure.mapper;

import com.tuan.ecommerce.modules.category.application.dto.CategoryResponse;
import com.tuan.ecommerce.modules.category.application.dto.CreateCategoryRequest;
import com.tuan.ecommerce.modules.category.application.dto.UpdateCategoryRequest;
import com.tuan.ecommerce.modules.category.domain.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(normalizeDescription(request.getDescription()));
        return category;
    }

    public void updateEntity(Category category, UpdateCategoryRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(normalizeDescription(request.getDescription()));
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream().map(this::toResponse).toList();
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
