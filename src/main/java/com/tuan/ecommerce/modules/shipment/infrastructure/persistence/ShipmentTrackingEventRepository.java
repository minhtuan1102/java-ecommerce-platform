package com.tuan.ecommerce.modules.shipment.infrastructure.persistence;

import com.tuan.ecommerce.modules.shipment.domain.ShipmentTrackingEvent;

import java.util.List;

public interface ShipmentTrackingEventRepository {
    ShipmentTrackingEvent save(ShipmentTrackingEvent event);
    List<ShipmentTrackingEvent> findByShipmentId(Long shipmentId);
}

