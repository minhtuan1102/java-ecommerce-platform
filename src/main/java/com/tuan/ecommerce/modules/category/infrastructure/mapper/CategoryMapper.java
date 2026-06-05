package com.tuan.ecommerce.modules.category.infrastructure.mapper;

import com.tuan.ecommerce.common.utils.SlugUtils;
import com.tuan.ecommerce.modules.category.application.dto.CategoryResponse;
import com.tuan.ecommerce.modules.category.application.dto.CreateCategoryRequest;
import com.tuan.ecommerce.modules.category.application.dto.UpdateCategoryRequest;
import com.tuan.ecommerce.modules.category.domain.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.makeSlug(request.getName());

        return Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(normalizeDescription(request.getDescription()))
                .active(true)
                .build();
    }

    public void updateEntity(Category category, UpdateCategoryRequest request) {
        category.setName(request.getName().trim());
        
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.makeSlug(request.getName());
        category.setSlug(slug);
        
        category.setDescription(normalizeDescription(request.getDescription()));
        
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
    }

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .active(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .children(toResponseList(category.getChildren())) // Chuyển đổi đệ quy cho danh mục con
                .build();
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
                .map(this::toResponseShort) // Dùng hàm short để tránh đệ quy vô hạn nếu không cần thiết, hoặc toResponse nếu muốn lấy cả cây
                .collect(Collectors.toList());
    }

    // Hàm bổ trợ để trả về response mà không đi sâu vào con của con (tùy nhu cầu)
    private CategoryResponse toResponseShort(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .active(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
    
    // Nếu muốn lấy cả cây đầy đủ
    public List<CategoryResponse> toResponseTree(List<Category> categories) {
        if (categories == null) return List.of();
        return categories.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
