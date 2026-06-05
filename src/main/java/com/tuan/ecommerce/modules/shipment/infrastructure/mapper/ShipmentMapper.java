package com.tuan.ecommerce.modules.shipment.infrastructure.mapper;

import com.tuan.ecommerce.modules.shipment.application.dto.ShipmentResponse;
import com.tuan.ecommerce.modules.shipment.application.dto.ShipmentTrackingResponse;
import com.tuan.ecommerce.modules.shipment.domain.Shipment;
import com.tuan.ecommerce.modules.shipment.domain.ShipmentTrackingEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipmentMapper {

    public ShipmentResponse toResponse(Shipment shipment, List<ShipmentTrackingEvent> events) {
        List<ShipmentTrackingResponse> trackingResponses = events.stream()
                .map(this::toTrackingResponse)
                .toList();

        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrder().getId())
                .status(shipment.getStatus())
                .carrier(shipment.getCarrier())
                .trackingNumber(shipment.getTrackingNumber())
                .shippingAddress(shipment.getShippingAddress())
                .phoneNumber(shipment.getPhoneNumber())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .trackingEvents(trackingResponses)
                .build();
    }

    private ShipmentTrackingResponse toTrackingResponse(ShipmentTrackingEvent event) {
        return ShipmentTrackingResponse.builder()
                .id(event.getId())
                .status(event.getStatus())
                .description(event.getDescription())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}

