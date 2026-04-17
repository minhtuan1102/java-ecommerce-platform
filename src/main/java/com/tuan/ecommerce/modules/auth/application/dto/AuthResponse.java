package com.tuan.ecommerce.modules.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime createdAt;
    private String message;
}
