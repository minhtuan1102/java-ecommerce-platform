package com.tuan.ecommerce.modules.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAddressRequest {
    private String label;

    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String fullAddress;

    private boolean defaultAddress;
}

