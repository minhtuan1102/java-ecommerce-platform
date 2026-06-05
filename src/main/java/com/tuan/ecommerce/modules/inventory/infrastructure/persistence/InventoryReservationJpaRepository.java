package com.tuan.ecommerce.modules.inventory.infrastructure.persistence;

import com.tuan.ecommerce.modules.inventory.domain.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByOrderId(Long orderId);
}

