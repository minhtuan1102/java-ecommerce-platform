package com.tuan.ecommerce.modules.brand.infrastructure.mapper;

import com.tuan.ecommerce.common.utils.SlugUtils;
import com.tuan.ecommerce.modules.brand.application.dto.BrandResponse;
import com.tuan.ecommerce.modules.brand.application.dto.CreateBrandRequest;
import com.tuan.ecommerce.modules.brand.application.dto.UpdateBrandRequest;
import com.tuan.ecommerce.modules.brand.domain.Brand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandMapper {
    public Brand toEntity (CreateBrandRequest request){
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.makeSlug(request.getName());

        return Brand.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(normalizeDescription(request.getDescription()))
                .logoUrl(normalizeText(request.getLogoUrl()))
                .logoPublicId(normalizeText(request.getLogoPublicId()))
                .active(true)
                .build();
    }

    public void updateEntity (Brand brand, UpdateBrandRequest request){
        brand.setName(request.getName().trim());
        
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.makeSlug(request.getName());
        brand.setSlug(slug);
        
        brand.setDescription(normalizeDescription(request.getDescription()));
        brand.setLogoUrl(normalizeText(request.getLogoUrl()));
        brand.setLogoPublicId(normalizeText(request.getLogoPublicId()));
        
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }
    }

    public BrandResponse toResponse (Brand brand){
        if (brand == null) return null;
        
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .logoPublicId(brand.getLogoPublicId())
                .active(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    public List<BrandResponse> toResponseList (List<Brand> brands){
        return brands.stream().map(this::toResponse).toList();
    }
    public String normalizeDescription (String description){
        return normalizeText(description);
    }

    public String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
