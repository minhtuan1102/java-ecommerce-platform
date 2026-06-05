package com.tuan.ecommerce.modules.wishlist.api;

import com.tuan.ecommerce.modules.product.application.dto.ProductResponse;
import com.tuan.ecommerce.modules.wishlist.application.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getWishlist(Principal principal) {
        return ResponseEntity.ok(wishlistService.getMyWishlist(principal.getName()));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getWishlistIds(Principal principal) {
        return ResponseEntity.ok(wishlistService.getMyWishlistProductIds(principal.getName()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<List<Long>> add(@PathVariable Long productId, Principal principal) {
        return ResponseEntity.ok(wishlistService.add(principal.getName(), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<List<Long>> remove(@PathVariable Long productId, Principal principal) {
        return ResponseEntity.ok(wishlistService.remove(principal.getName(), productId));
    }
}
