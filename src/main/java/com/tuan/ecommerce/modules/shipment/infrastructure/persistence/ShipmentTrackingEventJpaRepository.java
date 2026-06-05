package com.tuan.ecommerce.modules.shipment.infrastructure.persistence;

import com.tuan.ecommerce.modules.shipment.domain.ShipmentTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentTrackingEventJpaRepository extends JpaRepository<ShipmentTrackingEvent, Long> {
    List<ShipmentTrackingEvent> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId);
}

