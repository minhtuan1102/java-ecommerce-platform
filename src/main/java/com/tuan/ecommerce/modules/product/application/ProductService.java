package com.tuan.ecommerce.modules.product.application;

import com.tuan.ecommerce.common.dto.PageResponse;
import com.tuan.ecommerce.common.utils.SlugUtils;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import com.tuan.ecommerce.modules.brand.domain.Brand;
import com.tuan.ecommerce.modules.brand.infrastructure.persistence.BrandRepository;
import com.tuan.ecommerce.modules.cloudinary.application.CloudinaryService;
import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductImageRequest;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          BrandRepository brandRepository, ProductMapper productMapper,
                          CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productMapper = productMapper;
        this.cloudinaryService = cloudinaryService;
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

        product.setImages(mapImages(request, product));

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

        List<String> removedPublicIds = findRemovedPublicIds(product, request);
        product.getImages().clear();
        product.getImages().addAll(mapImages(request, product));

        syncSkus(product, request);

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
        removedPublicIds.forEach(cloudinaryService::deleteImage);
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
                                                            Long brandId,
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
        Page<Product> productPage = productRepository.searchProductsPage(name, categoryId, brandId, minPrice, maxPrice, pageable);
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

    private List<ProductImage> mapImages(CreateProductRequest request, Product product) {
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            return request.getImages().stream()
                    .filter(image -> image.getUrl() != null && !image.getUrl().isBlank())
                    .map(image -> ProductImage.builder()
                            .product(product)
                            .url(image.getUrl().trim())
                            .cloudinaryPublicId(image.getCloudinaryPublicId() != null ? image.getCloudinaryPublicId().trim() : null)
                            .main(Boolean.TRUE.equals(image.getMain()))
                            .sortOrder(image.getSortOrder())
                            .build())
                    .collect(Collectors.toList());
        }

        if (request.getImageUrls() == null) {
            return List.of();
        }

        return request.getImageUrls().stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> ProductImage.builder()
                        .product(product)
                        .url(url.trim())
                        .main(request.getImageUrls().indexOf(url) == 0)
                        .sortOrder(request.getImageUrls().indexOf(url))
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> findRemovedPublicIds(Product product, CreateProductRequest request) {
        Set<String> nextPublicIds = request.getImages() == null
                ? Set.of()
                : request.getImages().stream()
                        .map(ProductImageRequest::getCloudinaryPublicId)
                        .filter(publicId -> publicId != null && !publicId.isBlank())
                        .collect(Collectors.toSet());

        return product.getImages().stream()
                .map(ProductImage::getCloudinaryPublicId)
                .filter(publicId -> publicId != null && !publicId.isBlank())
                .filter(publicId -> !nextPublicIds.contains(publicId))
                .toList();
    }

    private void syncSkus(Product product, CreateProductRequest request) {
        Map<String, ProductSKU> existingByCode = product.getSkus().stream()
                .collect(Collectors.toMap(
                        sku -> normalizeSkuCode(sku.getSkuCode()),
                        sku -> sku,
                        (first, second) -> first
                ));

        Set<String> requestedCodes = request.getSkus().stream()
                .map(skuReq -> normalizeSkuCode(skuReq.getSkuCode()))
                .collect(Collectors.toSet());

        if (requestedCodes.size() != request.getSkus().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate SKU code in request");
        }

        product.getSkus().removeIf(sku -> !requestedCodes.contains(normalizeSkuCode(sku.getSkuCode())));

        for (var skuReq : request.getSkus()) {
            String normalizedCode = normalizeSkuCode(skuReq.getSkuCode());
            ProductSKU sku = existingByCode.get(normalizedCode);
            if (sku == null) {
                sku = ProductSKU.builder()
                        .product(product)
                        .skuCode(normalizedCode)
                        .status(ProductSkuStatus.ACTIVE)
                        .build();
                product.getSkus().add(sku);
            }

            sku.setProduct(product);
            sku.setSkuCode(normalizedCode);
            sku.setTierIndex(skuReq.getTierIndex());
            sku.setPrice(skuReq.getPrice());
            sku.setStock(skuReq.getStock());
            sku.setStatus(ProductSkuStatus.ACTIVE);
        }
    }

    private String normalizeSkuCode(String skuCode) {
        return skuCode == null ? "" : skuCode.trim();
    }
}
