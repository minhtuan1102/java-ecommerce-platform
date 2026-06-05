package com.tuan.ecommerce.modules.user.application.dto;

import lombok.Data;

@Data
public class UpdateAddressRequest {
    private String label;
    private String recipientName;
    private String phoneNumber;
    private String fullAddress;
    private Boolean defaultAddress;
}

