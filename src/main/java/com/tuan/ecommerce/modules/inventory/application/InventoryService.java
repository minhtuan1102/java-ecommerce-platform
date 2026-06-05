package com.tuan.ecommerce.modules.inventory.application;

import com.tuan.ecommerce.modules.cart.domain.CartItem;
import com.tuan.ecommerce.modules.inventory.domain.InventoryReservation;
import com.tuan.ecommerce.modules.inventory.domain.InventoryReservationStatus;
import com.tuan.ecommerce.modules.inventory.infrastructure.persistence.InventoryReservationRepository;
import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.domain.ProductSkuStatus;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductSkuRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private static final Duration DEFAULT_RESERVATION_TTL = Duration.ofMinutes(30);

    private final InventoryReservationRepository reservationRepository;
    private final ProductSkuRepository skuRepository;

    public InventoryService(InventoryReservationRepository reservationRepository,
                            ProductSkuRepository skuRepository) {
        this.reservationRepository = reservationRepository;
        this.skuRepository = skuRepository;
    }

    @Transactional
    public List<InventoryReservation> reserveForOrder(Order order, List<CartItem> items, String reservedBy) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(DEFAULT_RESERVATION_TTL);
        List<InventoryReservation> reservations = new ArrayList<>();

        for (CartItem cartItem : items) {
            ProductSKU sku = skuRepository.findByIdWithLock(cartItem.getSku().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found"));

            if (!sku.getProduct().isActive() || sku.getStatus() != ProductSkuStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is not available");
            }

            if (sku.getStock() < cartItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + sku.getProduct().getName() + " (" + sku.getTierIndex() + ") is out of stock");
            }

            sku.setStock(sku.getStock() - cartItem.getQuantity());
            skuRepository.save(sku);

            InventoryReservation reservation = InventoryReservation.builder()
                    .order(order)
                    .sku(sku)
                    .quantity(cartItem.getQuantity())
                    .status(InventoryReservationStatus.RESERVED)
                    .reservedBy(reservedBy)
                    .expiresAt(expiresAt)
                    .build();

            reservations.add(reservationRepository.save(reservation));
        }

        return reservations;
    }

    @Transactional
    public void commitReservations(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == InventoryReservationStatus.RESERVED) {
                reservation.setStatus(InventoryReservationStatus.COMMITTED);
                reservationRepository.save(reservation);
            }
        }
    }

    @Transactional
    public void releaseReservations(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != InventoryReservationStatus.RESERVED) {
                continue;
            }

            ProductSKU sku = skuRepository.findByIdWithLock(reservation.getSku().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found"));
            sku.setStock(sku.getStock() + reservation.getQuantity());
            skuRepository.save(sku);

            reservation.setStatus(InventoryReservationStatus.RELEASED);
            reservationRepository.save(reservation);
        }
    }
}

