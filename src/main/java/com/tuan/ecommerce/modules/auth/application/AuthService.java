package com.tuan.ecommerce.modules.auth.application;

import com.tuan.ecommerce.modules.auth.application.dto.AuthResponse;
import com.tuan.ecommerce.modules.auth.application.dto.LoginRequest;
import com.tuan.ecommerce.modules.auth.application.dto.RegisterRequest;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.AuthMapper;
import com.tuan.ecommerce.modules.auth.infrastructure.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;

    public AuthService(UserRepository userRepository, AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedUsername = request.getUsername().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = authMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return authMapper.toResponse(savedUser, "Register success");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return authMapper.toResponse(user, "Login success");
    }

    public void logout() {
        // Placeholder for refresh token invalidation when JWT is added.
    }
}
