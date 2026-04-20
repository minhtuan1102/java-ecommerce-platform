package com.tuan.ecommerce.modules.auth.application;

import com.tuan.ecommerce.modules.auth.application.dto.AdminUpdateUserRequest;
import com.tuan.ecommerce.modules.auth.application.dto.UpdateProfileRequest;
import com.tuan.ecommerce.modules.auth.application.dto.UserResponse;
import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserCrudService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;

    public UserCrudService(UserRepository userRepository,
                           RoleRepository roleRepository,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        User user = findByEmailOrThrow(email);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateMe(String email, UpdateProfileRequest request) {
        User user = findByEmailOrThrow(email);

        String normalizedUsername = request.getUsername().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(normalizedUsername, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteMe(String email) {
        User user = findByEmailOrThrow(email);
        anonymizeDeletedUser(user);
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !isDeleted(user))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (isDeleted(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUserById(Long id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (isDeleted(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String normalizedUsername = request.getUsername().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(normalizedUsername, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Set<Role> roles = request.getRoles().stream()
                .map(this::findRoleOrThrow)
                .collect(Collectors.toSet());

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        anonymizeDeletedUser(user);
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Role findRoleOrThrow(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleName));
    }

    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private boolean isDeleted(User user) {
        return user.getEmail() != null && user.getEmail().endsWith("@deleted.local");
    }

    private void anonymizeDeletedUser(User user) {
        String marker = UUID.randomUUID().toString().replace("-", "");
        user.setUsername("deleted_" + user.getId() + "_" + marker.substring(0, 8));
        user.setEmail("deleted_" + user.getId() + "_" + marker.substring(8, 16) + "@deleted.local");
        user.setPassword(marker);
        user.setRoles(Set.of());
    }
}


