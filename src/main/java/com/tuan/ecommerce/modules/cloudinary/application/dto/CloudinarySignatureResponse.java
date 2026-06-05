package com.tuan.ecommerce.modules.cloudinary.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinarySignatureResponse {
    private String cloudName;
    private String apiKey;
    private String signature;
}
