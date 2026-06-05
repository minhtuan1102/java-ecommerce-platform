package com.tuan.ecommerce.modules.shipment.infrastructure.persistence;

import com.tuan.ecommerce.modules.shipment.domain.Shipment;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ShipmentRepositoryAdapter implements ShipmentRepository {

    private final ShipmentJpaRepository jpaRepository;

    public ShipmentRepositoryAdapter(ShipmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Shipment save(Shipment shipment) {
        return jpaRepository.save(shipment);
    }

    @Override
    public Optional<Shipment> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Shipment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}

