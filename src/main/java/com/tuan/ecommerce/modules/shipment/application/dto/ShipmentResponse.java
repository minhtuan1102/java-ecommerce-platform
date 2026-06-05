package com.tuan.ecommerce.modules.shipment.application.dto;

import com.tuan.ecommerce.modules.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private Long orderId;
    private ShipmentStatus status;
    private String carrier;
    private String trackingNumber;
    private String shippingAddress;
    private String phoneNumber;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShipmentTrackingResponse> trackingEvents;
}

