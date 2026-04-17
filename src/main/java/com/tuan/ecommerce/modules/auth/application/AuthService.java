package com.tuan.ecommerce.modules.auth.application;

import com.tuan.ecommerce.modules.auth.application.dto.AuthResponse;
import com.tuan.ecommerce.modules.auth.application.dto.LoginRequest;
import com.tuan.ecommerce.modules.auth.application.dto.RegisterRequest;
import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.mapper.AuthMapper;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.security.JwtService;
import com.tuan.ecommerce.modules.auth.infrastructure.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, AuthMapper authMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role not found"));

        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(userRole));
        
        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(new CustomUserDetails(savedUser));
        return authMapper.toResponse(savedUser, jwtToken, null, "Register success");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()) && !user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return authMapper.toResponse(user, jwtToken, null, "Login success");
    }

    public void logout() {
        // Placeholder for refresh token invalidation
    }
}
