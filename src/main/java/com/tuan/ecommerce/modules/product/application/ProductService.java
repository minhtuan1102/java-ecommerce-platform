package com.tuan.ecommerce.modules.product.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.mapper.ProductMapper;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import com.tuan.ecommerce.modules.shop.infrastructure.persistence.ShopRepository;
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
        List<Product> products = productRepository.findByShopId(shopId);
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String name, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        List<Product> products = productRepository.searchProducts(name, categoryId, minPrice, maxPrice);
        return productMapper.toResponseList(products);
    }
}
