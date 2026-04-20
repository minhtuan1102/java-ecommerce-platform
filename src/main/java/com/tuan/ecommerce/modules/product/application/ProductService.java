package com.tuan.ecommerce.modules.product.application;

import com.tuan.ecommerce.common.dto.PageResponse;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.application.dto.ReviewProductRequest;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.mapper.ProductMapper;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import com.tuan.ecommerce.modules.shop.infrastructure.persistence.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          ShopRepository shopRepository, UserRepository userRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Shop shop = shopRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You must create a shop first before adding products"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Product product = productMapper.toEntity(request);
        product.setShop(shop);
        product.setCategory(category);
        product.setApprovalStatus(ProductApprovalStatus.PENDING);
        product.setReviewNote("Waiting for admin approval");
        product.setApprovedBy(null);
        product.setApprovedAt(null);

        // Map Images
        if (request.getImageUrls() != null) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .product(product)
                            .url(url)
                            .main(request.getImageUrls().indexOf(url) == 0) // Giả định cái đầu tiên là ảnh chính
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
                        .build())
                .collect(Collectors.toList());
        product.setSkus(skus);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, CreateProductRequest request, String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.getShop().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this product");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setBrand(request.getBrand() != null ? request.getBrand().trim() : null);
        product.setCategory(category);
        product.setApprovalStatus(ProductApprovalStatus.PENDING);
        product.setReviewNote("Product updated and waiting for admin approval");
        product.setApprovedBy(null);
        product.setApprovedAt(null);

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
                        .build())
                .collect(Collectors.toList());
        product.getSkus().addAll(skus);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId, String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.getShop().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this product");
        }

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
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByShop(Long shopId) {
        List<Product> products = productRepository.findByShopIdAndApprovalStatusAndActiveTrue(shopId, ProductApprovalStatus.APPROVED);
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String name, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        List<Product> products = productRepository.searchProducts(name, categoryId, minPrice, maxPrice);
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
        int normalizedSize = Math.max(15, Math.min(size, 20));
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

    @Transactional(readOnly = true)
    public List<ProductResponse> getPendingProductsForAdmin() {
        List<Product> products = productRepository.findByApprovalStatusAndActiveTrue(ProductApprovalStatus.PENDING);
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsForMyShop(String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Shop shop = shopRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        return productMapper.toResponseList(productRepository.findByShopId(shop.getId()));
    }

    @Transactional
    public ProductResponse approveProduct(Long productId, String adminEmail, ReviewProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot approve an inactive product");
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        product.setApprovalStatus(ProductApprovalStatus.APPROVED);
        product.setReviewNote(normalizeReviewNote(request != null ? request.getReviewNote() : null, "Approved"));
        product.setApprovedBy(admin);
        product.setApprovedAt(LocalDateTime.now());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse rejectProduct(Long productId, String adminEmail, ReviewProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reject an inactive product");
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        product.setApprovalStatus(ProductApprovalStatus.REJECTED);
        product.setReviewNote(normalizeReviewNote(request != null ? request.getReviewNote() : null, "Rejected"));
        product.setApprovedBy(admin);
        product.setApprovedAt(LocalDateTime.now());

        return productMapper.toResponse(productRepository.save(product));
    }

    private String normalizeReviewNote(String note, String fallback) {
        if (note == null || note.isBlank()) {
            return fallback;
        }
        return note.trim();
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
