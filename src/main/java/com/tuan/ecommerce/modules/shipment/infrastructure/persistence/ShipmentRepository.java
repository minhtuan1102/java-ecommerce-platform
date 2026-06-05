package com.tuan.ecommerce.modules.shipment.infrastructure.persistence;

import com.tuan.ecommerce.modules.shipment.domain.Shipment;

import java.util.Optional;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(Long id);
    Optional<Shipment> findByOrderId(Long orderId);
}

