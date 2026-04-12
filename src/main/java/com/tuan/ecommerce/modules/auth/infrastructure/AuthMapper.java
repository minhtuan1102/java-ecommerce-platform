package com.tuan.ecommerce.modules.auth.infrastructure;

import com.tuan.ecommerce.modules.auth.application.dto.AuthResponse;
import com.tuan.ecommerce.modules.auth.application.dto.RegisterRequest;
import com.tuan.ecommerce.modules.auth.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(request.getPassword());
        return user;
    }

    public AuthResponse toResponse(User user, String message) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        response.setMessage(message);
        return response;
    }
}

