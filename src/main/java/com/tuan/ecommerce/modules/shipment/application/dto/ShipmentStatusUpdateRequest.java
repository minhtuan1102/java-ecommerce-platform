package com.tuan.ecommerce.modules.shipment.application.dto;

import com.tuan.ecommerce.modules.shipment.domain.ShipmentStatus;
import lombok.Data;

@Data
public class ShipmentStatusUpdateRequest {
    private ShipmentStatus status;
    private String description;
}

