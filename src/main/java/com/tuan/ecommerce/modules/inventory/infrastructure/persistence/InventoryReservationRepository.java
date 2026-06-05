package com.tuan.ecommerce.modules.inventory.infrastructure.persistence;

import com.tuan.ecommerce.modules.inventory.domain.InventoryReservation;

import java.util.List;

public interface InventoryReservationRepository {
    InventoryReservation save(InventoryReservation reservation);
    List<InventoryReservation> findByOrderId(Long orderId);
}

