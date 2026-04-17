package com.tuan.ecommerce.modules.auth.infrastructure.mapper;

import com.tuan.ecommerce.modules.auth.application.dto.AuthResponse;
import com.tuan.ecommerce.modules.auth.application.dto.RegisterRequest;
import com.tuan.ecommerce.modules.auth.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(request.getPassword())
                .build();
    }

    public AuthResponse toResponse(User user, String accessToken, String refreshToken, String message) {
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername()) // This returns email because of UserDetails implementation
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .createdAt(user.getCreatedAt())
                .message(message)
                .build();
    }
}
