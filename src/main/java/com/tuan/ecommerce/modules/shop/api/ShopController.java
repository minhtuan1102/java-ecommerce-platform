package com.tuan.ecommerce.modules.shop.api;

import com.tuan.ecommerce.modules.shop.application.ShopService;
import com.tuan.ecommerce.modules.shop.application.dto.CreateShopRequest;
import com.tuan.ecommerce.modules.shop.application.dto.ShopResponse;
import com.tuan.ecommerce.modules.shop.application.dto.UpdateShopRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody CreateShopRequest request, Principal principal) {
        ShopResponse response = shopService.createShop(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ShopResponse> getMyShop(Principal principal) {
        return ResponseEntity.ok(shopService.getMyShop(principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }

    @PutMapping("/my-shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ShopResponse> updateMyShop(@Valid @RequestBody UpdateShopRequest request, Principal principal) {
        return ResponseEntity.ok(shopService.updateMyShop(request, principal.getName()));
    }

    @DeleteMapping("/my-shop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteMyShop(Principal principal) {
        shopService.deleteMyShop(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
