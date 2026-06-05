package com.tuan.ecommerce.modules.shipment.infrastructure.persistence;

import com.tuan.ecommerce.modules.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentJpaRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
}

