package com.tuan.ecommerce.modules.cloudinary.application;

import com.tuan.ecommerce.modules.cloudinary.application.dto.CloudinarySignatureResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {

    private static final Set<String> EXCLUDED_SIGNATURE_PARAMS = Set.of(
            "file", "resource_type", "cloud_name", "api_key", "signature"
    );

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient httpClient;

    public CloudinaryService(@Value("${cloudinary.cloud-name:}") String cloudName,
                             @Value("${cloudinary.api-key:}") String apiKey,
                             @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.httpClient = HttpClient.newHttpClient();
    }

    public CloudinarySignatureResponse signUploadParams(Map<String, String> paramsToSign) {
        ensureConfigured();
        return CloudinarySignatureResponse.builder()
                .cloudName(cloudName)
                .apiKey(apiKey)
                .signature(sign(paramsToSign))
                .build();
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank() || !isConfigured()) {
            return;
        }

        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, String> params = Map.of(
                "public_id", publicId,
                "timestamp", String.valueOf(timestamp)
        );

        String form = "public_id=" + encode(publicId)
                + "&timestamp=" + timestamp
                + "&api_key=" + encode(apiKey)
                + "&signature=" + encode(sign(params));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String sign(Map<String, String> params) {
        String payload = canonicalize(params) + apiSecret;
        return sha1(payload);
    }

    private String canonicalize(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .filter(entry -> !EXCLUDED_SIGNATURE_PARAMS.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> right,
                        TreeMap::new
                ))
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                hex.append(String.format("%02x", current));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 algorithm is not available", ex);
        }
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary is not configured");
        }
    }

    private boolean isConfigured() {
        return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
