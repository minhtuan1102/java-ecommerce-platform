package com.tuan.ecommerce.modules.shipment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingResponse {
    private Long id;
    private String status;
    private String description;
    private LocalDateTime occurredAt;
}

