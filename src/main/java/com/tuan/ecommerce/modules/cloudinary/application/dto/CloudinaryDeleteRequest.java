package com.tuan.ecommerce.modules.cloudinary.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloudinaryDeleteRequest {

    @NotBlank(message = "Cloudinary public ID is required")
    private String publicId;
}
