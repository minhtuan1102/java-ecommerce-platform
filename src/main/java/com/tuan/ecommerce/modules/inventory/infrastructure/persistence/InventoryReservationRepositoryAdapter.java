package com.tuan.ecommerce.modules.inventory.infrastructure.persistence;

import com.tuan.ecommerce.modules.inventory.domain.InventoryReservation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryReservationRepositoryAdapter implements InventoryReservationRepository {

    private final InventoryReservationJpaRepository jpaRepository;

    public InventoryReservationRepositoryAdapter(InventoryReservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public List<InventoryReservation> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}

