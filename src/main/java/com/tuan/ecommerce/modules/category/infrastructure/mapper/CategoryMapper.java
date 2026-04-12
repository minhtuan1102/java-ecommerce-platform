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
        return Category.builder()
                .name(request.getName().trim())
                .description(normalizeDescription(request.getDescription()))
                .build();
    }

    public void updateEntity(Category category, UpdateCategoryRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(normalizeDescription(request.getDescription()));
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
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
