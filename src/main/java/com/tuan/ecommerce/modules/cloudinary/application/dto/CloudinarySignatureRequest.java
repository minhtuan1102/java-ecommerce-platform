package com.tuan.ecommerce.modules.cloudinary.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class CloudinarySignatureRequest {

    @NotEmpty(message = "Cloudinary parameters are required")
    private Map<String, String> paramsToSign;
}
