package com.tuan.ecommerce.modules.shipment.infrastructure.persistence;

import com.tuan.ecommerce.modules.shipment.domain.ShipmentTrackingEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ShipmentTrackingEventRepositoryAdapter implements ShipmentTrackingEventRepository {

    private final ShipmentTrackingEventJpaRepository jpaRepository;

    public ShipmentTrackingEventRepositoryAdapter(ShipmentTrackingEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ShipmentTrackingEvent save(ShipmentTrackingEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<ShipmentTrackingEvent> findByShipmentId(Long shipmentId) {
        return jpaRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId);
    }
}

