package com.tuan.ecommerce.modules.product.application;

import com.tuan.ecommerce.common.dto.PageResponse;
import com.tuan.ecommerce.common.utils.SlugUtils;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import com.tuan.ecommerce.modules.brand.domain.Brand;
import com.tuan.ecommerce.modules.brand.infrastructure.persistence.BrandRepository;
import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.domain.*;
import com.tuan.ecommerce.modules.product.infrastructure.mapper.ProductMapper;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          BrandRepository brandRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setBrand(brand);
        product.setSlug(SlugUtils.makeSlug(request.getName()));
        product.setApprovalStatus(ProductApprovalStatus.APPROVED); // Tự động duyệt cho single vendor

        // Map Images
        if (request.getImageUrls() != null) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .product(product)
                            .url(url)
                            .main(request.getImageUrls().indexOf(url) == 0)
                            .build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        // Map SKUs
        List<ProductSKU> skus = request.getSkus().stream()
                .map(skuReq -> ProductSKU.builder()
                        .product(product)
                        .skuCode(skuReq.getSkuCode())
                        .tierIndex(skuReq.getTierIndex())
                        .price(skuReq.getPrice())
                        .stock(skuReq.getStock())
                        .status(ProductSkuStatus.ACTIVE)
                        .build())
                .collect(Collectors.toList());
        product.setSkus(skus);

        // Map Specs
        if (request.getSpecs() != null) {
            List<ProductSpec> specs = request.getSpecs().stream()
                    .map(specReq -> ProductSpec.builder()
                            .product(product)
                            .specKey(specReq.getKey())
                            .specValue(specReq.getValue())
                            .displayOrder(specReq.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            product.setSpecs(specs);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, CreateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));

        product.setName(request.getName().trim());
        product.setSlug(SlugUtils.makeSlug(request.getName()));
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setBrand(brand);
        product.setCategory(category);

        product.getImages().clear();
        if (request.getImageUrls() != null) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .product(product)
                            .url(url)
                            .main(request.getImageUrls().indexOf(url) == 0)
                            .build())
                    .collect(Collectors.toList());
            product.getImages().addAll(images);
        }

        product.getSkus().clear();
        List<ProductSKU> skus = request.getSkus().stream()
                .map(skuReq -> ProductSKU.builder()
                        .product(product)
                        .skuCode(skuReq.getSkuCode())
                        .tierIndex(skuReq.getTierIndex())
                        .price(skuReq.getPrice())
                        .stock(skuReq.getStock())
                        .status(ProductSkuStatus.ACTIVE)
                        .build())
                .collect(Collectors.toList());
        product.getSkus().addAll(skus);

        product.getSpecs().clear();
        if (request.getSpecs() != null) {
            List<ProductSpec> specs = request.getSpecs().stream()
                    .map(specReq -> ProductSpec.builder()
                            .product(product)
                            .specKey(specReq.getKey())
                            .specValue(specReq.getValue())
                            .displayOrder(specReq.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            product.getSpecs().addAll(specs);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProductsPage(String name,
                                                            Long categoryId,
                                                            java.math.BigDecimal minPrice,
                                                            java.math.BigDecimal maxPrice,
                                                            int page,
                                                            int size,
                                                            String sortBy,
                                                            String sortDir) {
        int normalizedSize = Math.max(1, Math.min(size, 100));
        int normalizedPage = Math.max(page, 0);
        String normalizedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(direction, normalizedSortBy));
        Page<Product> productPage = productRepository.searchProductsPage(name, categoryId, minPrice, maxPrice, pageable);
        List<ProductResponse> content = productMapper.toResponseList(productPage.getContent());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        return switch (sortBy) {
            case "name", "createdAt", "updatedAt" -> sortBy;
            default -> "createdAt";
        };
    }
}
