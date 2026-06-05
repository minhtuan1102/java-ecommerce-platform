package com.tuan.ecommerce.modules.brand.api;

import com.tuan.ecommerce.modules.brand.application.dto.BrandResponse;
import com.tuan.ecommerce.modules.brand.application.dto.CreateBrandRequest;
import com.tuan.ecommerce.modules.brand.application.dto.UpdateBrandRequest;
import com.tuan.ecommerce.modules.brand.application.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public List<BrandResponse> getBrands() {
        return brandService.getBrands();
    }

    @GetMapping("/{id}")
    public BrandResponse getBrandById(@PathVariable Long id) {
        return brandService.getBrandById(id);
    }

    @GetMapping("/slug/{slug}")
    public BrandResponse getBrandBySlug(@PathVariable String slug) {
        return brandService.getBrandBySlug(slug);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse updateBrand(@PathVariable Long id, @Valid @RequestBody UpdateBrandRequest request) {
        return brandService.updateBrand(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
