package com.tuan.ecommerce.modules.product.api;

import com.tuan.ecommerce.common.dto.PageResponse;
import com.tuan.ecommerce.modules.product.application.ProductService;
import com.tuan.ecommerce.modules.product.application.dto.CreateProductRequest;
import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.product.application.dto.ReviewProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // --- PUBLIC ENDPOINTS (Cho người mua) ---

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.searchProducts(name, categoryId, minPrice, maxPrice));
    }

    @GetMapping("/discovery")
    public ResponseEntity<PageResponse<ProductResponse>> getDiscoveryProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(productService.searchProductsPage(
                name, categoryId, minPrice, maxPrice, page, size, sortBy, sortDir
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ProductResponse>> getProductsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getProductsByShop(shopId));
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getPendingProducts() {
        return ResponseEntity.ok(productService.getPendingProductsForAdmin());
    }

    @GetMapping("/my-shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ProductResponse>> getMyShopProducts(Principal principal) {
        return ResponseEntity.ok(productService.getProductsForMyShop(principal.getName()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> approveProduct(@PathVariable Long id,
                                                          @RequestBody(required = false) ReviewProductRequest request,
                                                          Principal principal) {
        return ResponseEntity.ok(productService.approveProduct(id, principal.getName(), request));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> rejectProduct(@PathVariable Long id,
                                                         @RequestBody(required = false) ReviewProductRequest request,
                                                         Principal principal) {
        return ResponseEntity.ok(productService.rejectProduct(id, principal.getName(), request));
    }

    // --- SELLER ENDPOINTS (Cho người bán) ---

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request, Principal principal) {
        ProductResponse response = productService.createProduct(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody CreateProductRequest request,
                                                         Principal principal) {
        return ResponseEntity.ok(productService.updateProduct(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Principal principal) {
        productService.deleteProduct(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
