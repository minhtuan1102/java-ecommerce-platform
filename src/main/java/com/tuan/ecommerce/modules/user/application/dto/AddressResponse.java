package com.tuan.ecommerce.modules.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String label;
    private String recipientName;
    private String phoneNumber;
    private String fullAddress;
    private boolean defaultAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

