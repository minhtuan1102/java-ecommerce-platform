package com.tuan.ecommerce.modules.user.api;

import com.tuan.ecommerce.modules.user.application.UserAddressService;
import com.tuan.ecommerce.modules.user.application.dto.AddressResponse;
import com.tuan.ecommerce.modules.user.application.dto.CreateAddressRequest;
import com.tuan.ecommerce.modules.user.application.dto.UpdateAddressRequest;
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
@RequestMapping("/api/v1/addresses")
public class UserAddressController {

    private final UserAddressService addressService;

    public UserAddressController(UserAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(Principal principal) {
        return ResponseEntity.ok(addressService.getMyAddresses(principal.getName()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody CreateAddressRequest request, Principal principal) {
        AddressResponse response = addressService.createAddress(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long addressId,
                                                        @RequestBody UpdateAddressRequest request,
                                                        Principal principal) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, request, principal.getName()));
    }

    @PutMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Long addressId, Principal principal) {
        return ResponseEntity.ok(addressService.setDefaultAddress(addressId, principal.getName()));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId, Principal principal) {
        addressService.deleteAddress(addressId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

