package com.tuan.ecommerce.modules.brand.application.service;

import com.tuan.ecommerce.modules.brand.application.dto.BrandResponse;
import com.tuan.ecommerce.modules.brand.application.dto.CreateBrandRequest;
import com.tuan.ecommerce.modules.brand.application.dto.UpdateBrandRequest;
import com.tuan.ecommerce.modules.brand.domain.Brand;
import com.tuan.ecommerce.modules.brand.infrastructure.mapper.BrandMapper;
import com.tuan.ecommerce.modules.brand.infrastructure.persistence.BrandRepository;
import com.tuan.ecommerce.modules.cloudinary.application.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final CloudinaryService cloudinaryService;

    public BrandService(BrandRepository brandRepository, BrandMapper brandMapper, CloudinaryService cloudinaryService) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        String normalizedName = request.getName().trim();
        if (brandRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand name already exists");
        }
        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toResponse(savedBrand);
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getBrands() {
        return brandMapper.toResponseList(brandRepository.findAll());
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
        return brandMapper.toResponse(brand);
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with slug: " + slug));
        return brandMapper.toResponse(brand);
    }

    @Transactional
    public BrandResponse updateBrand(Long id, UpdateBrandRequest request) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
        
        String normalizedName = request.getName().trim();
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand name already exists");
        }
        
        String previousPublicId = existingBrand.getLogoPublicId();
        brandMapper.updateEntity(existingBrand, request);
        Brand savedBrand = brandRepository.save(existingBrand);
        if (previousPublicId != null
                && !previousPublicId.isBlank()
                && (savedBrand.getLogoPublicId() == null || !previousPublicId.equals(savedBrand.getLogoPublicId()))) {
            cloudinaryService.deleteImage(previousPublicId);
        }
        return brandMapper.toResponse(savedBrand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));
        String publicId = existingBrand.getLogoPublicId();
        brandRepository.delete(existingBrand);
        cloudinaryService.deleteImage(publicId);
    }
}
