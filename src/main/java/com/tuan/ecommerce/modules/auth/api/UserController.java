package com.tuan.ecommerce.modules.auth.api;

import com.tuan.ecommerce.modules.auth.application.UserCrudService;
import com.tuan.ecommerce.modules.auth.application.dto.AdminUpdateUserRequest;
import com.tuan.ecommerce.modules.auth.application.dto.UpdateProfileRequest;
import com.tuan.ecommerce.modules.auth.application.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserCrudService userCrudService;

    public UserController(UserCrudService userCrudService) {
        this.userCrudService = userCrudService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMe(Principal principal) {
        return ResponseEntity.ok(userCrudService.getMe(principal.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request, Principal principal) {
        return ResponseEntity.ok(userCrudService.updateMe(principal.getName(), request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMe(Principal principal) {
        userCrudService.deleteMe(principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userCrudService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userCrudService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserById(@PathVariable Long id,
                                                       @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userCrudService.updateUserById(id, request));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userCrudService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}

