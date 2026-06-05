package com.tuan.ecommerce.modules.cloudinary.api;

import com.tuan.ecommerce.modules.cloudinary.application.CloudinaryService;
import com.tuan.ecommerce.modules.cloudinary.application.dto.CloudinaryDeleteRequest;
import com.tuan.ecommerce.modules.cloudinary.application.dto.CloudinarySignatureRequest;
import com.tuan.ecommerce.modules.cloudinary.application.dto.CloudinarySignatureResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cloudinary")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/signature")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CloudinarySignatureResponse> signUpload(@Valid @RequestBody CloudinarySignatureRequest request) {
        return ResponseEntity.ok(cloudinaryService.signUploadParams(request.getParamsToSign()));
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@Valid @RequestBody CloudinaryDeleteRequest request) {
        cloudinaryService.deleteImage(request.getPublicId());
        return ResponseEntity.noContent().build();
    }
}
