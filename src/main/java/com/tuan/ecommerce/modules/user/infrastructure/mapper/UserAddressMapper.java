package com.tuan.ecommerce.modules.user.infrastructure.mapper;

import com.tuan.ecommerce.modules.user.application.dto.AddressResponse;
import com.tuan.ecommerce.modules.user.application.dto.CreateAddressRequest;
import com.tuan.ecommerce.modules.user.application.dto.UpdateAddressRequest;
import com.tuan.ecommerce.modules.user.domain.UserAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAddressMapper {

    public UserAddress toEntity(CreateAddressRequest request) {
        return UserAddress.builder()
                .label(trimOrNull(request.getLabel()))
                .recipientName(trimOrNull(request.getRecipientName()))
                .phoneNumber(request.getPhoneNumber().trim())
                .fullAddress(request.getFullAddress().trim())
                .defaultAddress(request.isDefaultAddress())
                .build();
    }

    public void updateEntity(UserAddress address, UpdateAddressRequest request) {
        if (request.getLabel() != null) {
            address.setLabel(trimOrNull(request.getLabel()));
        }
        if (request.getRecipientName() != null) {
            address.setRecipientName(trimOrNull(request.getRecipientName()));
        }
        if (request.getPhoneNumber() != null) {
            address.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getFullAddress() != null) {
            address.setFullAddress(request.getFullAddress().trim());
        }
        if (request.getDefaultAddress() != null) {
            address.setDefaultAddress(request.getDefaultAddress());
        }
    }

    public AddressResponse toResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .fullAddress(address.getFullAddress())
                .defaultAddress(address.isDefaultAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    public List<AddressResponse> toResponseList(List<UserAddress> addresses) {
        return addresses.stream().map(this::toResponse).toList();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

